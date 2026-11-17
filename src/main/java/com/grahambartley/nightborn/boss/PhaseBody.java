package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.util.Identifier;

public record PhaseBody(Map<Identifier, Double> attributes, float maxHit, boolean glowing) {

  public static final PhaseBody PLAIN = new PhaseBody(Map.of(), 0.0F, false);

  public static final Codec<PhaseBody> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.unboundedMap(Identifier.CODEC, Codec.doubleRange(-64.0, 64.0))
                          .optionalFieldOf("attributes", Map.of())
                          .forGetter(PhaseBody::attributes),
                      Codec.floatRange(0.0F, 1000.0F)
                          .optionalFieldOf("max_hit", 0.0F)
                          .forGetter(PhaseBody::maxHit),
                      Codec.BOOL.optionalFieldOf("glowing", false).forGetter(PhaseBody::glowing))
                  .apply(instance, PhaseBody::new));

  public PhaseBody {
    attributes = Map.copyOf(attributes);
  }

  public float limit(final float amount, final boolean uncapped) {
    return uncapped || maxHit <= 0.0F ? amount : Math.min(amount, maxHit);
  }

  public double bonus(final Identifier attribute) {
    return attributes.getOrDefault(attribute, 0.0);
  }

  public static double blend(final double from, final double to, final double progress) {
    return from + (to - from) * Math.max(0.0, Math.min(1.0, progress));
  }
}
