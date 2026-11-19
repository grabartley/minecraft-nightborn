package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LiningTest {

  private static final int DIAMOND_DURABILITY = 33;

  private static double sum(
      final AttributeModifiersComponent modifiers, final RegistryEntry<EntityAttribute> attribute) {
    return modifiers.modifiers().stream()
        .filter(entry -> entry.attribute().equals(attribute))
        .mapToDouble(entry -> entry.modifier().value())
        .sum();
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("a lined piece is as strong as diamond and keeps its dye, enchantments and name")
  void linesLikeDiamond(final ClothingPiece piece) {
    final ItemStack stack = new ItemStack(ClothingItems.of(piece));
    stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x6B1A2A, true));
    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Mourning Coat"));
    stack.set(DataComponentTypes.REPAIR_COST, 4);
    stack.setDamage(3);

    final ItemStack lined = Lining.line(stack, ArmorMaterials.DIAMOND.value(), DIAMOND_DURABILITY);

    assertTrue(Lining.lined(lined));
    assertEquals(1, lined.getCount());
    assertEquals(stack.getItem(), lined.getItem());
    assertEquals(
        stack.get(DataComponentTypes.DYED_COLOR), lined.get(DataComponentTypes.DYED_COLOR));
    assertEquals(
        stack.get(DataComponentTypes.CUSTOM_NAME), lined.get(DataComponentTypes.CUSTOM_NAME));
    assertEquals(4, lined.get(DataComponentTypes.REPAIR_COST));
    assertEquals(3, lined.getDamage());
    assertEquals(piece.type().getMaxDamage(DIAMOND_DURABILITY), lined.getMaxDamage());
    final AttributeModifiersComponent modifiers = lined.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
    assertEquals(
        ArmorMaterials.DIAMOND.value().getProtection(piece.type()),
        sum(modifiers, EntityAttributes.GENERIC_ARMOR));
    assertEquals(
        ArmorMaterials.DIAMOND.value().toughness(),
        sum(modifiers, EntityAttributes.GENERIC_ARMOR_TOUGHNESS),
        1.0E-6);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("gothic clothing can be lined once, and never twice")
  void linedOnce(final ClothingPiece piece) {
    final ItemStack stack = new ItemStack(ClothingItems.of(piece));
    assertTrue(Lining.lineable(stack));
    assertFalse(
        Lining.lineable(Lining.line(stack, ArmorMaterials.DIAMOND.value(), DIAMOND_DURABILITY)));
  }

  @ParameterizedTest(name = "worn to {0} at a multiplier of {1}")
  @CsvSource({"3, 33", "200, 2", "500, 1"})
  @DisplayName("a lined piece never has more wear than its new durability allows")
  void wearFitsTheNewDurability(final int worn, final int multiplier) {
    final ItemStack stack = new ItemStack(ClothingItems.of(ClothingPiece.TRICORNE));
    stack.set(DataComponentTypes.MAX_DAMAGE, 1000);
    stack.setDamage(worn);
    final ItemStack lined = Lining.line(stack, ArmorMaterials.DIAMOND.value(), multiplier);
    assertEquals(ArmorItem.Type.HELMET.getMaxDamage(multiplier), lined.getMaxDamage());
    assertEquals(Math.min(worn, lined.getMaxDamage() - 1), lined.getDamage());
  }

  @Test
  @DisplayName("nothing but gothic clothing can be lined")
  void onlyClothing() {
    assertFalse(Lining.lineable(new ItemStack(Items.LEATHER_CHESTPLATE)));
    assertFalse(Lining.lineable(new ItemStack(Items.DIAMOND_SWORD)));
    assertTrue(
        Lining.line(new ItemStack(Items.STICK), ArmorMaterials.DIAMOND.value(), DIAMOND_DURABILITY)
            .isEmpty());
  }
}
