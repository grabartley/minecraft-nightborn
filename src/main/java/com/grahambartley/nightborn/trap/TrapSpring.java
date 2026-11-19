package com.grahambartley.nightborn.trap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;

public record TrapSpring(Identifier sound, ParticleEffect particle, int count) {

  public static final Codec<TrapSpring> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("sound").forGetter(TrapSpring::sound),
                      ParticleTypes.TYPE_CODEC.fieldOf("particle").forGetter(TrapSpring::particle),
                      Codec.intRange(0, 64)
                          .optionalFieldOf("count", 8)
                          .forGetter(TrapSpring::count))
                  .apply(instance, TrapSpring::new));
}
