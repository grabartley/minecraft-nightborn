package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

public final class Lining {

  public static final ComponentType<Unit> LINED =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(Nightborn.MOD_ID, "lined"),
          ComponentType.<Unit>builder()
              .codec(Unit.CODEC)
              .packetCodec(PacketCodec.unit(Unit.INSTANCE))
              .build());

  public static final RecipeSerializer<LiningRecipe> RECIPE =
      Registry.register(
          Registries.RECIPE_SERIALIZER,
          Identifier.of(Nightborn.MOD_ID, "smithing_lining"),
          new LiningRecipe.Serializer());

  private Lining() {}

  public static void register() {}

  public static boolean lined(final ItemStack stack) {
    return stack.contains(LINED);
  }

  public static boolean lineable(final ItemStack stack) {
    return stack.getItem() instanceof ClothingItem && !lined(stack);
  }

  public static ItemStack line(
      final ItemStack piece, final ArmorMaterial material, final int durabilityMultiplier) {
    if (!(piece.getItem() instanceof ArmorItem armor)) {
      return ItemStack.EMPTY;
    }
    final ItemStack lined = piece.copyWithCount(1);
    lined.set(LINED, Unit.INSTANCE);
    lined.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, strength(armor.getType(), material));
    lined.set(DataComponentTypes.MAX_DAMAGE, armor.getType().getMaxDamage(durabilityMultiplier));
    return lined;
  }

  public static AttributeModifiersComponent strength(
      final ArmorItem.Type type, final ArmorMaterial material) {
    final Identifier id = Identifier.ofVanilla("armor." + type.getName());
    final AttributeModifierSlot slot =
        AttributeModifierSlot.forEquipmentSlot(type.getEquipmentSlot());
    final AttributeModifiersComponent.Builder builder =
        AttributeModifiersComponent.builder()
            .add(
                EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(
                    id, material.getProtection(type), EntityAttributeModifier.Operation.ADD_VALUE),
                slot)
            .add(
                EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                new EntityAttributeModifier(
                    id, material.toughness(), EntityAttributeModifier.Operation.ADD_VALUE),
                slot);
    if (material.knockbackResistance() > 0.0F) {
      builder.add(
          EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
          new EntityAttributeModifier(
              id, material.knockbackResistance(), EntityAttributeModifier.Operation.ADD_VALUE),
          slot);
    }
    return builder.build();
  }
}
