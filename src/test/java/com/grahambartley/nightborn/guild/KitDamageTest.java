package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class KitDamageTest {

  private static HunterKit kit(final boolean ranged, final float damage) {
    return kit(ranged, damage, 0.0F);
  }

  private static HunterKit kit(final boolean ranged, final float damage, final float rankDamage) {
    return new HunterKit(
        Identifier.ofVanilla(ranged ? "crossbow" : "iron_sword"),
        ranged ? Optional.of(Identifier.of("nightborn", "stake_bolt")) : Optional.empty(),
        Optional.empty(),
        false,
        1,
        damage,
        rankDamage);
  }

  private static ItemStack held(final String item) {
    return switch (item) {
      case "kit" -> new ItemStack(Items.IRON_SWORD);
      case "other" -> new ItemStack(Items.STICK);
      default -> ItemStack.EMPTY;
    };
  }

  @ParameterizedTest(name = "damage {0}, holding {1}: {2}")
  @CsvSource({
    "6.0, kit, 6.0",
    "11.0, kit, 11.0",
    "0.0, kit, 0.0",
    "6.0, other, 0.0",
    "6.0, empty, 0.0"
  })
  @DisplayName("a hand weapon kit adds its damage only to a blow struck with that weapon")
  void blowWithTheKitWeapon(final float damage, final String holding, final float dealt) {
    assertEquals(dealt, KitDamage.blow(kit(false, damage), held(holding), 0));
  }

  @ParameterizedTest(name = "ranged {0}, damage {1}: bolt {2}")
  @CsvSource({"true, 4.0, 4.0", "true, 0.0, 0.0", "false, 6.0, 0.0"})
  @DisplayName("only a crossbow kit adds its damage to the bolts it looses")
  void boltForACrossbowKit(final boolean ranged, final float damage, final float bolt) {
    assertEquals(bolt, KitDamage.bolt(kit(ranged, damage), 0));
  }

  @ParameterizedTest(name = "a crossbow kit holding {0}")
  @CsvSource({"kit", "other", "empty"})
  @DisplayName("a crossbow kit adds nothing to a blow")
  void noBlowForACrossbowKit(final String holding) {
    assertEquals(0.0F, KitDamage.blow(kit(true, 4.0F), held(holding), 0));
  }

  @ParameterizedTest(name = "holding {0}")
  @CsvSource({"kit", "empty"})
  @DisplayName("a hunter with no kit adds nothing")
  void noKit(final String holding) {
    assertEquals(0.0F, KitDamage.blow(null, held(holding), 0));
    assertEquals(0.0F, KitDamage.bolt(null, 0));
  }

  @ParameterizedTest(name = "vampire {0}, damage {1}: {2}")
  @CsvSource({"true, 6.0, 6.0", "true, 11.0, 11.0", "false, 6.0, 0.0", "false, 11.0, 0.0"})
  @DisplayName("a kit's damage lands on vampires only")
  void vampiresOnly(final boolean vampire, final float damage, final float dealt) {
    assertEquals(dealt, KitDamage.against(vampire, damage));
  }

  @ParameterizedTest(name = "ranged {0}, damage {1} and {2} a rank, rank {3}: {4}")
  @CsvSource({
    "false, 1.5, 0.0, 3, 1.5",
    "false, 1.5, 0.5, 2, 2.5",
    "true, 0.8, 1.4, 0, 0.8",
    "true, 0.8, 1.4, 3, 5.0"
  })
  @DisplayName("a kit's damage grows with its hunter's rank by the step its data gives")
  void growsWithRank(
      final boolean ranged,
      final float damage,
      final float rankDamage,
      final int rank,
      final float dealt) {
    final HunterKit kit = kit(ranged, damage, rankDamage);
    assertEquals(
        dealt,
        ranged ? KitDamage.bolt(kit, rank) : KitDamage.blow(kit, held("kit"), rank),
        0.0001F);
  }
}
