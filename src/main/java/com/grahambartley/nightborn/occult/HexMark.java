package com.grahambartley.nightborn.occult;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record HexMark(UUID caster, float healing, long until) {

  public static final Codec<HexMark> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.INT_STREAM_CODEC.fieldOf("caster").forGetter(HexMark::caster),
                      Codec.FLOAT.fieldOf("healing").forGetter(HexMark::healing),
                      Codec.LONG.fieldOf("until").forGetter(HexMark::until))
                  .apply(instance, HexMark::new));

  public float heal(final float amount, final long now) {
    return now < until ? amount * healing : amount;
  }
}
