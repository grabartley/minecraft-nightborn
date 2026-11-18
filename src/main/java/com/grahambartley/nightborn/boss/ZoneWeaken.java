package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ZoneWeaken(String source, int strength, int ticks) {

  public static final Codec<ZoneWeaken> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.fieldOf("source").forGetter(ZoneWeaken::source),
                      Codec.intRange(1, 100).fieldOf("strength").forGetter(ZoneWeaken::strength),
                      Codec.intRange(1, 6000).fieldOf("ticks").forGetter(ZoneWeaken::ticks))
                  .apply(instance, ZoneWeaken::new));
}
