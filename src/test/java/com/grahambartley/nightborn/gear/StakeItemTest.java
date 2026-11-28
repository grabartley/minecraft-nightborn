package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.eldest.AshenSilver;
import com.grahambartley.nightborn.eldest.EldestSpoils;
import java.util.stream.Stream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StakeItemTest {

  static Stream<Arguments> stakes() {
    return Stream.of(
        Arguments.of(GearItems.WOODEN_STAKE, ToolMaterials.WOOD, 0.0),
        Arguments.of(GearItems.SILVER_STAKE, SilverMaterial.SILVER, 1.0),
        Arguments.of(EldestSpoils.ASHEN_SILVER_STAKE, AshenSilver.ASHEN_SILVER, 1.5));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("stakes")
  @DisplayName("a stake adds half its material's damage to a bare fist, quicker than a sword")
  void attributes(final Item stake, final ToolMaterial material, final double damage) {
    final AttributeModifiersComponent modifiers =
        stake.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
    assertEquals(damage, sum(modifiers, EntityAttributes.GENERIC_ATTACK_DAMAGE));
    assertEquals(
        (double) StakeItem.ATTACK_SPEED, sum(modifiers, EntityAttributes.GENERIC_ATTACK_SPEED));
    assertEquals(
        material.getDurability(), stake.getComponents().get(DataComponentTypes.MAX_DAMAGE));
  }

  private static double sum(
      final AttributeModifiersComponent modifiers, final RegistryEntry<EntityAttribute> attribute) {
    return modifiers.modifiers().stream()
        .filter(entry -> entry.attribute().equals(attribute))
        .mapToDouble(entry -> entry.modifier().value())
        .sum();
  }
}
