package com.grahambartley.nightborn.accessory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public record AccessoryHold(List<Item> items, Optional<RegistryEntry<Potion>> potion, int count) {

  public static final int MOST = 256;

  public static final Codec<AccessoryHold> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Registries.ITEM
                          .getCodec()
                          .listOf()
                          .fieldOf("items")
                          .forGetter(AccessoryHold::items),
                      Registries.POTION
                          .getEntryCodec()
                          .optionalFieldOf("potion")
                          .forGetter(AccessoryHold::potion),
                      Codec.intRange(1, MOST).fieldOf("count").forGetter(AccessoryHold::count))
                  .apply(instance, AccessoryHold::new));

  public AccessoryHold(final List<Item> items, final int count) {
    this(items, Optional.empty(), count);
  }

  public static Optional<AccessoryHold> fitting(
      final List<AccessoryHold> holds, final ItemStack stack) {
    return holds.stream().filter(hold -> hold.accepts(stack)).findFirst();
  }

  public static int capacity(final List<AccessoryHold> holds) {
    return holds.stream().mapToInt(AccessoryHold::count).sum();
  }

  public boolean accepts(final ItemStack stack) {
    return !stack.isEmpty()
        && this.items.contains(stack.getItem())
        && this.potion
            .map(
                wanted ->
                    stack
                        .getOrDefault(
                            DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)
                        .matches(wanted))
            .orElse(true);
  }

  public int room(final int held) {
    return Math.max(0, this.count - held);
  }
}
