package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringIdentifiable;

public enum Curve implements StringIdentifiable {
  BETWEEN,
  RISING,
  FALLING;

  public static final Codec<Curve> CODEC = StringIdentifiable.createCodec(Curve::values);

  public double value(final double x, final double low, final double high) {
    if (Double.isNaN(x)) {
      return 0.0;
    }
    return switch (this) {
      case BETWEEN -> x >= low && x <= high ? 1.0 : 0.0;
      case RISING -> rise(x, low, high);
      case FALLING -> 1.0 - rise(x, low, high);
    };
  }

  private static double rise(final double x, final double low, final double high) {
    if (high <= low) {
      return x >= high ? 1.0 : 0.0;
    }
    return Math.clamp((x - low) / (high - low), 0.0, 1.0);
  }

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
