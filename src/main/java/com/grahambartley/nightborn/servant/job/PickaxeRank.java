package com.grahambartley.nightborn.servant.job;

import java.util.Comparator;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;

public final class PickaxeRank {

  public static final Comparator<ItemStack> BEST_FIRST = Comparator.comparingInt(PickaxeRank::of);

  private PickaxeRank() {}

  public static int of(final ItemStack stack) {
    return stack.getItem() instanceof ToolItem tool ? tool.getMaterial().getDurability() : 0;
  }
}
