package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.config.GearSettings;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VampireBaneTest {

  private static final GearSettings SETTINGS = new GearSettings(4, 3, 6, 40, 10, 60, 5, 3, 10);

  @ParameterizedTest(name = "vampire {0}, stake {1}, silver {2}: +{3}")
  @CsvSource({
    "true, true, false, 4",
    "true, false, true, 3",
    "true, true, true, 7",
    "true, false, false, 0",
    "false, true, true, 0",
    "false, false, true, 0"
  })
  @DisplayName("stakes and silver add their damage against vampires only")
  void bonus(final boolean vampire, final boolean stake, final boolean silver, final float bonus) {
    assertEquals(bonus, VampireBane.bonus(vampire, stake, silver, SETTINGS));
  }

  @ParameterizedTest(name = "base {0}, bonus {1}, speed {2}: {3}")
  @CsvSource({
    "2.0, 4.0, 3.2, 3.25",
    "2.0, 4.0, 1.0, 6.0",
    "2.0, 0.0, 3.0, 2.0",
    "2.0, 4.0, 0.0, 2.0"
  })
  @DisplayName("a bolt's bonus is spread over its speed so it lands as a flat bonus")
  void projectileBase(
      final double base, final float bonus, final double speed, final double expected) {
    assertEquals(expected, VampireBane.projectileBase(base, bonus, speed), 1.0e-9);
  }

  @Test
  @DisplayName("a blow with no projectile behind it carries no ammo")
  void noAmmo() {
    assertTrue(
        VampireBane.ammo(new DamageSource(RegistryEntry.of(new DamageType("generic", 0.0F))))
            .isEmpty());
  }
}
