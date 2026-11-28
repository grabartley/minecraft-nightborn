package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class KitDamageTest {

  private static HunterKit kit(final boolean ranged, final float damage) {
    return new HunterKit(
        Identifier.of("nightborn", "weapon"),
        ranged ? Optional.of(Identifier.of("nightborn", "bolt")) : Optional.empty(),
        Optional.empty(),
        false,
        1,
        damage);
  }

  @ParameterizedTest(name = "ranged {0}, damage {1}: melee {2}, bolt {3}")
  @CsvSource({
    "false, 6.0, 6.0, 0.0",
    "false, 11.0, 11.0, 0.0",
    "false, 0.0, 0.0, 0.0",
    "true, 4.0, 0.0, 4.0",
    "true, 0.0, 0.0, 0.0"
  })
  @DisplayName(
      "a kit's damage lands on its own weapon: the blow for a hand weapon, the bolt for a crossbow")
  void landsOnItsWeapon(
      final boolean ranged, final float damage, final float melee, final float bolt) {
    final HunterKit kit = kit(ranged, damage);
    assertEquals(melee, KitDamage.melee(kit));
    assertEquals(bolt, KitDamage.bolt(kit));
  }

  @Test
  @DisplayName("a hunter with no kit gains nothing")
  void noKit() {
    assertEquals(0.0F, KitDamage.melee(null));
    assertEquals(0.0F, KitDamage.bolt(null));
  }
}
