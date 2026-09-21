package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.TacticalGameEngine
import com.example.game.TileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Vanguard Ops", appName)
  }

  @Test
  fun `game engine initial state and round credits`() {
    val engine = TacticalGameEngine()
    assertEquals(2800, engine.roundCredits)
    assertEquals(100, engine.playerHp)
    assertEquals(100, engine.playerArmor)
    assertTrue(engine.isPlayerAlive)
  }

  @Test
  fun `firing weapon decreases magazine ammo and applies recoil`() {
    val engine = TacticalGameEngine()
    val initialAmmo = engine.currentMagAmmo
    val fired = engine.fireWeapon(weaponDamage = 34, weaponRecoil = 1.2f)
    assertTrue(fired)
    assertEquals(initialAmmo - 1, engine.currentMagAmmo)
    assertTrue(engine.weaponRecoilOffset > 0f)
  }

  @Test
  fun `destructible environment initialized with crates and barrels`() {
    val engine = TacticalGameEngine()
    assertTrue(engine.destructibles.isNotEmpty())
    val anyCrate = engine.destructibles.values.find { it.type == TileType.WOODEN_CRATE }
    assertNotNull(anyCrate)
    assertEquals(60, anyCrate?.currentHp)
  }
}
