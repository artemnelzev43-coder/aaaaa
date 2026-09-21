package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.math.log10
import kotlin.math.sqrt

class RealVoiceChatManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isTransmitting = MutableStateFlow(false)
    val isTransmitting: StateFlow<Boolean> = _isTransmitting.asStateFlow()

    private val _isReceivingVoice = MutableStateFlow(false)
    val isReceivingVoice: StateFlow<Boolean> = _isReceivingVoice.asStateFlow()

    private val _lastSpeakerName = MutableStateFlow("Рация")
    val lastSpeakerName: StateFlow<String> = _lastSpeakerName.asStateFlow()

    private val _inputAudioLevel = MutableStateFlow(0)
    val inputAudioLevel: StateFlow<Int> = _inputAudioLevel.asStateFlow()

    private var recordJob: Job? = null
    private var playbackJob: Job? = null
    private var receiveTimeoutJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var rxSocket: DatagramSocket? = null

    private var targetPeerIp: String = "255.255.255.255"
    private var currentLocalPlayerName: String = "Player"

    companion object {
        const val VOICE_PORT = 8889
        const val SAMPLE_RATE = 16000
        const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val BUFFER_SIZE = 1024
        private const val TAG = "RealVoiceChat"
    }

    fun setTargetPeerIp(ip: String) {
        targetPeerIp = ip
    }

    fun startListening(localPlayerName: String = "Player") {
        currentLocalPlayerName = localPlayerName
        if (playbackJob?.isActive == true) return

        playbackJob = scope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_OUT,
                    AUDIO_ENCODING
                ).coerceAtLeast(BUFFER_SIZE * 2)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AUDIO_ENCODING)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL_OUT)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                rxSocket?.close()
                rxSocket = try {
                    DatagramSocket(VOICE_PORT).apply {
                        broadcast = true
                        reuseAddress = true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Socket bind: ${e.message}")
                    null
                }

                val buffer = ByteArray(BUFFER_SIZE * 2 + 64)
                val packet = DatagramPacket(buffer, buffer.size)

                while (isActive) {
                    try {
                        rxSocket?.receive(packet)
                        val length = packet.length
                        if (length > 20) {
                            val header = String(buffer, 0, 4)
                            if (header == "VOX_") {
                                val speakerBytes = ByteArray(16)
                                System.arraycopy(buffer, 4, speakerBytes, 0, 16)
                                val speaker = String(speakerBytes).trimEnd { it <= ' ' || it.code == 0 }

                                // Don't echo own voice
                                if (speaker != currentLocalPlayerName) {
                                    _lastSpeakerName.value = speaker
                                    _isReceivingVoice.value = true

                                    receiveTimeoutJob?.cancel()
                                    receiveTimeoutJob = scope.launch {
                                        delay(1500L)
                                        _isReceivingVoice.value = false
                                    }

                                    val pcmOffset = 20
                                    val pcmLength = length - pcmOffset
                                    if (pcmLength > 0) {
                                        audioTrack?.write(buffer, pcmOffset, pcmLength)
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Playback loop error: ${e.message}")
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                    audioTrack = null
                    rxSocket?.close()
                    rxSocket = null
                } catch (_: Exception) {}
            }
        }
    }

    fun stopListening() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
            rxSocket?.close()
            rxSocket = null
        } catch (_: Exception) {}
        _isReceivingVoice.value = false
    }

    @SuppressLint("MissingPermission")
    fun startTransmitting(localPlayerName: String = "Player") {
        currentLocalPlayerName = localPlayerName
        if (_isTransmitting.value) return
        _isTransmitting.value = true

        recordJob?.cancel()
        recordJob = scope.launch {
            var txSocket: DatagramSocket? = null
            try {
                val minBufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_IN,
                    AUDIO_ENCODING
                ).coerceAtLeast(BUFFER_SIZE * 2)

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_IN,
                    AUDIO_ENCODING,
                    minBufferSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    _isTransmitting.value = false
                    return@launch
                }

                audioRecord?.startRecording()
                txSocket = DatagramSocket().apply { broadcast = true }

                val pcmBuffer = ShortArray(BUFFER_SIZE)
                val sendBytes = ByteArray(BUFFER_SIZE * 2 + 20)

                // Header: "VOX_" + 16-byte fixed length speaker name
                sendBytes[0] = 'V'.code.toByte()
                sendBytes[1] = 'O'.code.toByte()
                sendBytes[2] = 'X'.code.toByte()
                sendBytes[3] = '_'.code.toByte()

                val nameBytes = currentLocalPlayerName.toByteArray(Charsets.UTF_8)
                for (i in 0 until 16) {
                    sendBytes[4 + i] = if (i < nameBytes.size) nameBytes[i] else 0
                }

                val destAddress = try {
                    InetAddress.getByName(targetPeerIp)
                } catch (_: Exception) {
                    InetAddress.getByName("255.255.255.255")
                }

                while (isActive && _isTransmitting.value) {
                    val readSamples = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                    if (readSamples > 0) {
                        var sum = 0.0
                        for (i in 0 until readSamples) {
                            val sample = pcmBuffer[i].toInt()
                            sum += sample * sample
                            val byteIdx = 20 + i * 2
                            sendBytes[byteIdx] = (sample and 0xFF).toByte()
                            sendBytes[byteIdx + 1] = ((sample shr 8) and 0xFF).toByte()
                        }
                        val rms = sqrt(sum / readSamples)
                        val db = if (rms > 1) (20 * log10(rms)).toInt().coerceIn(0, 95) else 0
                        _inputAudioLevel.value = db

                        val packet = DatagramPacket(
                            sendBytes,
                            20 + readSamples * 2,
                            destAddress,
                            VOICE_PORT
                        )
                        try {
                            txSocket?.send(packet)
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Recording error: ${e.message}")
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                    audioRecord = null
                    txSocket?.close()
                } catch (_: Exception) {}
                _isTransmitting.value = false
                _inputAudioLevel.value = 0
            }
        }
    }

    fun stopTransmitting() {
        _isTransmitting.value = false
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (_: Exception) {}
        _inputAudioLevel.value = 0
    }

    fun release() {
        stopTransmitting()
        stopListening()
    }
}
