package com.grahambartley.nightborn.occult;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;

public record FogPower(float radius, int durationTicks, List<StatusEffectInstance> effects) {

  public static final Codec<FogPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 16.0F).fieldOf("radius").forGetter(FogPower::radius),
                      Codec.intRange(20, 1200)
                          .fieldOf("duration_ticks")
                          .forGetter(FogPower::durationTicks),
                      StatusEffectInstance.CODEC
                          .listOf()
                          .fieldOf("effects")
                          .forGetter(FogPower::effects))
                  .apply(instance, FogPower::new));
}
