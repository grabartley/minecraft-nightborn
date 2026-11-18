package com.grahambartley.nightborn.mob.look;

import com.grahambartley.nightborn.accessory.AccessorySlot;
import com.grahambartley.nightborn.clothing.Dressed;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public final class OutfitDressing {

  private OutfitDressing() {}

  public static Optional<EquipmentSlot> slot(final Item item) {
    return item instanceof ArmorItem armor
        ? Optional.of(armor.getSlotType())
        : AccessorySlot.fitting(new ItemStack(item)).map(AccessorySlot::body);
  }

  public static Map<EquipmentSlot, ItemStack> stacks(
      final VampireOutfit outfit, final Function<Identifier, Optional<Item>> items) {
    final Map<EquipmentSlot, ItemStack> stacks = new EnumMap<>(EquipmentSlot.class);
    for (final VampireOutfit.Piece piece : outfit.pieces()) {
      items
          .apply(piece.item())
          .ifPresent(
              item ->
                  slot(item)
                      .ifPresent(slot -> stacks.putIfAbsent(slot, stack(item, piece.color()))));
    }
    return stacks;
  }

  private static ItemStack stack(final Item item, final Optional<Integer> color) {
    final ItemStack stack = new ItemStack(item);
    if (item instanceof Dressed dressed
        ? dressed.garment().dyeable()
        : stack.isIn(ItemTags.DYEABLE)) {
      color.ifPresent(
          rgb -> stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(rgb, true)));
    }
    return stack;
  }
}
