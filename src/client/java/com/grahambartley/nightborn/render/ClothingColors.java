package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.clothing.ClothingItem;
import com.grahambartley.nightborn.clothing.ClothingItems;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ColorHelper;

public final class ClothingColors {

  static final int DYED_LAYER = 0;
  static final int UNTINTED = -1;

  private ClothingColors() {}

  public static void register() {
    ClothingItems.all().stream()
        .filter(item -> item.piece().dyeable())
        .forEach(item -> ColorProviderRegistry.ITEM.register(ClothingColors::tint, item));
  }

  private static int tint(final ItemStack stack, final int layer) {
    return tint(layer, ((ClothingItem) stack.getItem()).color(stack));
  }

  static int tint(final int layer, final int color) {
    return layer == DYED_LAYER ? ColorHelper.Argb.fullAlpha(color) : UNTINTED;
  }
}
