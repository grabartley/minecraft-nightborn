package com.grahambartley.nightborn.slayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record DownBlow(float below, Identifier upTo) {

  public static final Codec<DownBlow> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, 1.0F).fieldOf("below").forGetter(DownBlow::below),
                      Identifier.CODEC.fieldOf("up_to").forGetter(DownBlow::upTo))
                  .apply(instance, DownBlow::new));
}
