package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record PhaseVoice(Identifier sound, int everyTicks, float pitch) {

  public static final Codec<PhaseVoice> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("sound").forGetter(PhaseVoice::sound),
                      Codec.intRange(10, 6000)
                          .fieldOf("every_ticks")
                          .forGetter(PhaseVoice::everyTicks),
                      Codec.floatRange(0.5F, 2.0F)
                          .optionalFieldOf("pitch", 1.0F)
                          .forGetter(PhaseVoice::pitch))
                  .apply(instance, PhaseVoice::new));

  public boolean speaksAt(final long sincePhaseBegan) {
    return sincePhaseBegan > 0 && sincePhaseBegan % everyTicks == 0;
  }
}
