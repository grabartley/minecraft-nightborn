package com.grahambartley.nightborn.mob.look;

import com.grahambartley.nightborn.clothing.ClothingItem;
import com.grahambartley.nightborn.mob.VampireEntity;
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
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public final class OutfitDressing {

  private OutfitDressing() {}

  public static Optional<EquipmentSlot> slot(final Item item) {
    return item instanceof ArmorItem armor ? Optional.of(armor.getSlotType()) : Optional.empty();
  }

  public static void dress(final VampireEntity vampire, final DynamicRegistryManager registries) {
    final Map<Identifier, VampireOutfit> outfits = VampireLooks.outfits(registries);
    Appearance.pick(
            VampireLooks.looks(registries),
            outfits,
            VampireLooks.builds(registries),
            vampire.getRandom()::nextDouble)
        .ifPresent(
            appearance -> {
              vampire.setAppearance(appearance.look(), appearance.height(), appearance.width());
              appearance
                  .outfit()
                  .map(outfits::get)
                  .map(outfit -> stacks(outfit, VampireLooks::item))
                  .ifPresent(stacks -> stacks.forEach(vampire::equipStack));
            });
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
    if (item instanceof ClothingItem clothing
        ? clothing.piece().dyeable()
        : stack.isIn(ItemTags.DYEABLE)) {
      color.ifPresent(
          rgb -> stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(rgb, true)));
    }
    return stack;
  }
}
