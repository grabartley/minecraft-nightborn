package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.blood.GiftModifier;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.faction.Faction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryGiftsTest {

  private static final Identifier RING = Identifier.of("nightborn", "garnet_signet");
  private static final GiftModifier SLOWER =
      new GiftModifier(
          WornAttributes.BLOOD_DRAIN, -0.1, EntityAttributeModifier.Operation.ADD_VALUE);

  @ParameterizedTest(name = "works for {0}, worn by {1}: {2} gifts")
  @CsvSource({"VAMPIRE, VAMPIRE, 1", "VAMPIRE, HUMAN, 0", ", HUMAN, 1"})
  @DisplayName("a gift applies only to the side it works for")
  void wanted(final Faction only, final Faction wearer, final int gifts) {
    assertEquals(
        gifts,
        AccessoryGifts.wanted(
                Map.of(RING, new Accessory(Optional.ofNullable(only), List.of(SLOWER))), wearer)
            .size());
  }

  @Test
  @DisplayName("two of the same piece share one modifier id, so the gift counts once")
  void sameIdForSamePiece() {
    assertEquals(
        "nightborn:accessory/nightborn/garnet_signet/0",
        AccessoryGifts.modifierId(RING, 0).toString());
  }

  @ParameterizedTest(name = "{0}: stray {1}")
  @CsvSource({
    "nightborn:accessory/nightborn/garnet_signet/0, false",
    "nightborn:accessory/nightborn/garnet_signet/1, true",
    "nightborn:skill/x/0/0, false",
    "minecraft:accessory/nightborn/garnet_signet/1, false"
  })
  @DisplayName("only accessory modifiers that are no longer worn are taken away")
  void stray(final String modifier, final boolean stray) {
    assertEquals(
        stray,
        AccessoryGifts.stray(
            Identifier.of(modifier),
            WornAttributes.BLOOD_DRAIN,
            Map.of(AccessoryGifts.modifierId(RING, 0), SLOWER)));
  }

  @Test
  @DisplayName("a worn id moved to another attribute is stray on the old one")
  void strayOnOtherAttribute() {
    assertEquals(
        true,
        AccessoryGifts.stray(
            AccessoryGifts.modifierId(RING, 0),
            WornAttributes.SUN_DAMAGE,
            Map.of(AccessoryGifts.modifierId(RING, 0), SLOWER)));
  }
}
