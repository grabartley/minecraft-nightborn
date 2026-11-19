package com.grahambartley.nightborn.trap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FieldWeakening(String source, String cue) {

  public static final Codec<FieldWeakening> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.string(1, 64).fieldOf("source").forGetter(FieldWeakening::source),
                      Codec.string(1, 128).fieldOf("cue").forGetter(FieldWeakening::cue))
                  .apply(instance, FieldWeakening::new));
}
