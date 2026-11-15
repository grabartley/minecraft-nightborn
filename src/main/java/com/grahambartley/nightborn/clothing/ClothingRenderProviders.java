package com.grahambartley.nightborn.clothing;

import java.util.function.Function;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

public final class ClothingRenderProviders {

  private static Function<ClothingItem, GeoRenderProvider> factory =
      item -> GeoRenderProvider.DEFAULT;

  private ClothingRenderProviders() {}

  public static void use(final Function<ClothingItem, GeoRenderProvider> source) {
    factory = source;
  }

  static GeoRenderProvider forItem(final ClothingItem item) {
    return factory.apply(item);
  }
}
