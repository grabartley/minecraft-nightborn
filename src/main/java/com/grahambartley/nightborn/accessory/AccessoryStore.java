package com.grahambartley.nightborn.accessory;

import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.sound.SoundEvents;

public final class AccessoryStore {

  private AccessoryStore() {}

  public static BundleContentsComponent contents(final ItemStack store) {
    return store.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
  }

  public static Optional<TooltipData> tooltip(final ItemStack store) {
    final BundleContentsComponent contents = contents(store);
    return contents.isEmpty() ? Optional.empty() : Optional.of(new BundleTooltipData(contents));
  }

  public static int held(final ItemStack store) {
    return contents(store).stream().mapToInt(ItemStack::getCount).sum();
  }

  public static int room(
      final AccessoryHold hold, final ItemStack store, final ItemStack incoming) {
    return hold.accepts(incoming) ? Math.min(incoming.getCount(), hold.room(held(store))) : 0;
  }

  public static int put(final AccessoryHold hold, final ItemStack store, final ItemStack incoming) {
    final int room = room(hold, store, incoming);
    if (room <= 0) {
      return 0;
    }
    final BundleContentsComponent.Builder builder =
        new BundleContentsComponent.Builder(contents(store));
    final int added = builder.add(incoming.copyWithCount(room));
    incoming.decrement(added);
    store.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
    return added;
  }

  public static Optional<ItemStack> take(final ItemStack store) {
    final BundleContentsComponent contents = contents(store);
    if (contents.isEmpty()) {
      return Optional.empty();
    }
    final BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(contents);
    final ItemStack taken = builder.removeFirst();
    store.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
    return Optional.ofNullable(taken).filter(stack -> !stack.isEmpty());
  }

  public static void soundPut(final Entity by) {
    by.playSound(
        SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + by.getWorld().getRandom().nextFloat() * 0.4F);
  }

  public static void soundTake(final Entity by) {
    by.playSound(
        SoundEvents.ITEM_BUNDLE_REMOVE_ONE,
        0.8F,
        0.8F + by.getWorld().getRandom().nextFloat() * 0.4F);
  }
}
