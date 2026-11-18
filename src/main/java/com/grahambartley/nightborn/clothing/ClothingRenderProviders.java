package com.grahambartley.nightborn.clothing;

import java.util.function.Function;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

public final class ClothingRenderProviders {

  private static Function<Dressed, GeoRenderProvider> factory = item -> GeoRenderProvider.DEFAULT;

  private ClothingRenderProviders() {}

  public static void use(final Function<Dressed, GeoRenderProvider> source) {
    factory = source;
  }

  public static GeoRenderProvider forItem(final Dressed item) {
    return factory.apply(item);
  }
}
