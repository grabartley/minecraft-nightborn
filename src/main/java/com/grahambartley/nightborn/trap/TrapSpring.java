package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.skill.PowerSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public record TrapSpring(PowerSound sound, ParticleEffect particle, int count) {

  public static final Codec<TrapSpring> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      PowerSound.CODEC.fieldOf("sound").forGetter(TrapSpring::sound),
                      ParticleTypes.TYPE_CODEC.fieldOf("particle").forGetter(TrapSpring::particle),
                      Codec.intRange(0, 64)
                          .optionalFieldOf("count", 8)
                          .forGetter(TrapSpring::count))
                  .apply(instance, TrapSpring::new));
}
