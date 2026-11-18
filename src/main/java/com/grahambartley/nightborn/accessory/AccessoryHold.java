package com.grahambartley.nightborn.accessory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public record AccessoryHold(List<Item> items, int count) {

  public static final int MOST = 64;

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
                      Codec.intRange(1, MOST).fieldOf("count").forGetter(AccessoryHold::count))
                  .apply(instance, AccessoryHold::new));

  public boolean accepts(final ItemStack stack) {
    return !stack.isEmpty() && this.items.contains(stack.getItem());
  }

  public int room(final int held) {
    return Math.max(0, this.count - held);
  }
}
