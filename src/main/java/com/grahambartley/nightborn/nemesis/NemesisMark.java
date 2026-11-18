package com.grahambartley.nightborn.nemesis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record NemesisMark(UUID quarry, int token, int growths, String given) {

  public static final Codec<NemesisMark> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.INT_STREAM_CODEC.fieldOf("quarry").forGetter(NemesisMark::quarry),
                      Codec.INT.optionalFieldOf("token", 0).forGetter(NemesisMark::token),
                      Codec.INT.optionalFieldOf("growths", 0).forGetter(NemesisMark::growths),
                      Codec.STRING.optionalFieldOf("given", "").forGetter(NemesisMark::given))
                  .apply(instance, NemesisMark::new));

  public NemesisMark {
    growths = Math.max(0, growths);
  }

  public NemesisMark grown() {
    return new NemesisMark(quarry, token, growths + 1, given);
  }

  public NemesisMark withToken(final int next) {
    return new NemesisMark(quarry, next, growths, given);
  }
}
