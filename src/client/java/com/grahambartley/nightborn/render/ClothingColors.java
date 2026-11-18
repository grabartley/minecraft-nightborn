package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.accessory.AccessoryItems;
import com.grahambartley.nightborn.clothing.ClothingItems;
import com.grahambartley.nightborn.clothing.Dressed;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ColorHelper;

public final class ClothingColors {

  static final int DYED_LAYER = 0;
  static final int UNTINTED = -1;

  private ClothingColors() {}

  public static void register() {
    Stream.<Item>concat(ClothingItems.all().stream(), AccessoryItems.all().stream())
        .filter(item -> ((Dressed) item).garment().dyeable())
        .forEach(item -> ColorProviderRegistry.ITEM.register(ClothingColors::tint, item));
  }

  private static int tint(final ItemStack stack, final int layer) {
    return tint(layer, ((Dressed) stack.getItem()).color(stack));
  }

  static int tint(final int layer, final int color) {
    return layer == DYED_LAYER ? ColorHelper.Argb.fullAlpha(color) : UNTINTED;
  }
}
