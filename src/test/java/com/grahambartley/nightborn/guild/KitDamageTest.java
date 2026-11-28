package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
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

  @ParameterizedTest(name = "ranged {0}, damage {1}: blow {2}, bolt {3}")
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
      final boolean ranged, final float damage, final float blow, final float bolt) {
    final HunterKit kit = kit(ranged, damage);
    assertEquals(blow, KitDamage.of(kit, false));
    assertEquals(bolt, KitDamage.of(kit, true));
  }

  @ParameterizedTest(name = "bolt {0}")
  @CsvSource({"true", "false"})
  @DisplayName("a hunter with no kit adds nothing")
  void noKit(final boolean bolt) {
    assertEquals(0.0F, KitDamage.of(null, bolt));
  }

  @ParameterizedTest(name = "vampire {0}, damage {1}: {2}")
  @CsvSource({"true, 6.0, 6.0", "true, 11.0, 11.0", "false, 6.0, 0.0", "false, 11.0, 0.0"})
  @DisplayName("a kit's damage lands on vampires only")
  void vampiresOnly(final boolean vampire, final float damage, final float dealt) {
    assertEquals(dealt, KitDamage.against(vampire, damage));
  }
}
