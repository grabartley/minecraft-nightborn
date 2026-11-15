package com.grahambartley.nightborn.mob.look;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VampireBuild(float width, float minHeight, float maxHeight, int weight) {

  public static final float MIN_WIDTH = 0.5F;
  public static final float MAX_WIDTH = 1.5F;
  public static final float MIN_HEIGHT = 1.0F;
  public static final float MAX_HEIGHT = 3.0F;

  public static final Codec<VampireBuild> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(MIN_WIDTH, MAX_WIDTH)
                          .fieldOf("width")
                          .forGetter(VampireBuild::width),
                      Codec.floatRange(MIN_HEIGHT, MAX_HEIGHT)
                          .fieldOf("min_height")
                          .forGetter(VampireBuild::minHeight),
                      Codec.floatRange(MIN_HEIGHT, MAX_HEIGHT)
                          .fieldOf("max_height")
                          .forGetter(VampireBuild::maxHeight),
                      Codec.intRange(1, VampireLook.MAX_WEIGHT)
                          .optionalFieldOf("weight", 1)
                          .forGetter(VampireBuild::weight))
                  .apply(instance, VampireBuild::new));

  public float height(final double roll) {
    final float low = Math.min(minHeight, maxHeight);
    final float high = Math.max(minHeight, maxHeight);
    return low + (float) (Math.max(0.0, Math.min(1.0, roll)) * (high - low));
  }
}
