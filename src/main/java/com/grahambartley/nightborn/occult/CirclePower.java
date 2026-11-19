package com.grahambartley.nightborn.occult;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

public record CirclePower(
    float radius,
    int durationTicks,
    float damage,
    List<StatusEffectInstance> effects,
    boolean stopsPrayers,
    Optional<Identifier> look) {

  public static final Codec<CirclePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 16.0F)
                          .fieldOf("radius")
                          .forGetter(CirclePower::radius),
                      Codec.intRange(20, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(CirclePower::durationTicks),
                      Codec.floatRange(0.0F, 20.0F)
                          .optionalFieldOf("damage", 0.0F)
                          .forGetter(CirclePower::damage),
                      StatusEffectInstance.CODEC
                          .listOf()
                          .optionalFieldOf("effects", List.of())
                          .forGetter(CirclePower::effects),
                      Codec.BOOL
                          .optionalFieldOf("stops_prayers", false)
                          .forGetter(CirclePower::stopsPrayers),
                      Identifier.CODEC.optionalFieldOf("look").forGetter(CirclePower::look))
                  .apply(instance, CirclePower::new));

  public CirclePower {
    effects = List.copyOf(effects);
  }
}
