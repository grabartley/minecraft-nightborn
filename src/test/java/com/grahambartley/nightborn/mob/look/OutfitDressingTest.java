package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class OutfitDressingTest {

  @ParameterizedTest(name = "{0} goes in {1}")
  @CsvSource({
    "nightborn:tricorne, HEAD",
    "nightborn:frock_coat, CHEST",
    "nightborn:long_skirt, LEGS",
    "nightborn:riding_boots, FEET",
    "minecraft:leather_helmet, HEAD",
    "minecraft:stone, "
  })
  @DisplayName("only wearable armour and clothing has a slot")
  void slot(final String item, final EquipmentSlot slot) {
    assertEquals(
        Optional.ofNullable(slot), OutfitDressing.slot(Registries.ITEM.get(Identifier.of(item))));
  }

  @Test
  @DisplayName("an outfit becomes one stack per slot, dyed where the cloth takes dye")
  void stacks() {
    final VampireOutfit outfit =
        new VampireOutfit(
            VampireRig.MALE,
            Identifier.of("nightborn", "georgian"),
            List.of(
                piece("nightborn:frock_coat", 0x5A1A24),
                piece("nightborn:buckled_shoes", 0x123456),
                piece("nightborn:doublet", 0x111111),
                piece("nightborn:no_such_thing", 0x222222)),
            1);
    final Map<EquipmentSlot, ItemStack> stacks =
        OutfitDressing.stacks(outfit, id -> Registries.ITEM.getOrEmpty(id));
    assertEquals(2, stacks.size());
    assertEquals(
        0x5A1A24, stacks.get(EquipmentSlot.CHEST).get(DataComponentTypes.DYED_COLOR).rgb());
    final DyedColorComponent shoes =
        stacks.get(EquipmentSlot.FEET).get(DataComponentTypes.DYED_COLOR);
    assertNull(shoes);
  }

  private static VampireOutfit.Piece piece(final String item, final int rgb) {
    return new VampireOutfit.Piece(Identifier.of(item), Optional.of(rgb));
  }
}
