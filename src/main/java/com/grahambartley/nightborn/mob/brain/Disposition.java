package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;

public record Disposition(Map<Aim, Float> aims, float thrift) {

  public static final float MOST_LEAN = 4.0F;

  public static final Disposition EVEN = new Disposition(Map.of(), 1.0F);

  private static final Codec<Float> LEAN = Codec.floatRange(0.0F, MOST_LEAN);

  public static final Codec<Disposition> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.unboundedMap(Aim.CODEC, LEAN)
                          .optionalFieldOf("aims", Map.of())
                          .forGetter(Disposition::aims),
                      LEAN.optionalFieldOf("thrift", 1.0F).forGetter(Disposition::thrift))
                  .apply(instance, Disposition::new));

  public Disposition {
    aims = Map.copyOf(aims);
  }

  public double lean(final Aim aim) {
    return aims.getOrDefault(aim, 1.0F);
  }
}
