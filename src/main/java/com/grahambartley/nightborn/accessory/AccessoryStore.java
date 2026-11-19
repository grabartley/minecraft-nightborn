package com.grahambartley.nightborn.accessory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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

  public static int heldIn(final AccessoryHold hold, final ItemStack store) {
    return contents(store).stream().filter(hold::accepts).mapToInt(ItemStack::getCount).sum();
  }

  public static int room(
      final List<AccessoryHold> holds, final ItemStack store, final ItemStack incoming) {
    return AccessoryHold.fitting(holds, incoming)
        .map(hold -> Math.min(incoming.getCount(), hold.room(heldIn(hold, store))))
        .orElse(0);
  }

  public static int put(
      final List<AccessoryHold> holds, final ItemStack store, final ItemStack incoming) {
    final int room = room(holds, store, incoming);
    if (room <= 0) {
      return 0;
    }
    store.set(DataComponentTypes.BUNDLE_CONTENTS, merged(contents(store), incoming, room));
    incoming.decrement(room);
    return room;
  }

  public static Optional<ItemStack> take(final ItemStack store) {
    return takeFirst(store, stack -> true, Integer.MAX_VALUE);
  }

  public static Optional<ItemStack> takeOne(
      final ItemStack store, final Predicate<ItemStack> wanted) {
    return takeFirst(store, wanted, 1);
  }

  public static int remove(
      final ItemStack store, final Predicate<ItemStack> wanted, final int count) {
    int removed = 0;
    while (removed < count) {
      final Optional<ItemStack> taken = takeFirst(store, wanted, count - removed);
      if (taken.isEmpty()) {
        break;
      }
      removed += taken.get().getCount();
    }
    return removed;
  }

  static BundleContentsComponent merged(
      final BundleContentsComponent contents, final ItemStack incoming, final int count) {
    final List<ItemStack> stacks = new ArrayList<>();
    contents.iterateCopy().forEach(stacks::add);
    int left = count;
    for (final ItemStack held : stacks) {
      if (left <= 0) {
        break;
      }
      if (ItemStack.areItemsAndComponentsEqual(held, incoming)) {
        final int moved = Math.min(left, held.getMaxCount() - held.getCount());
        held.increment(moved);
        left -= moved;
      }
    }
    while (left > 0) {
      final int moved = Math.min(left, incoming.getMaxCount());
      stacks.addFirst(incoming.copyWithCount(moved));
      left -= moved;
    }
    return new BundleContentsComponent(stacks);
  }

  private static Optional<ItemStack> takeFirst(
      final ItemStack store, final Predicate<ItemStack> wanted, final int most) {
    final List<ItemStack> stacks = new ArrayList<>();
    contents(store).iterateCopy().forEach(stacks::add);
    for (int i = 0; i < stacks.size(); i++) {
      final ItemStack held = stacks.get(i);
      if (wanted.test(held)) {
        final ItemStack taken = held.split(Math.min(most, held.getCount()));
        if (held.isEmpty()) {
          stacks.remove(i);
        }
        store.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(stacks));
        return Optional.of(taken);
      }
    }
    return Optional.empty();
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
