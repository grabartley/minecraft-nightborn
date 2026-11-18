package com.grahambartley.nightborn.lord;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.intprovider.IntProvider;

public record SummonPower(
    RegistryEntry<EntityType<?>> entity,
    IntProvider count,
    float spread,
    float rise,
    Optional<Integer> lifetimeTicks,
    Optional<Float> health,
    Optional<Float> damage) {

  public static final int MOST = 8;

  public static final Codec<SummonPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Registries.ENTITY_TYPE
                          .getEntryCodec()
                          .fieldOf("entity")
                          .forGetter(SummonPower::entity),
                      IntProvider.createValidatingCodec(1, MOST)
                          .fieldOf("count")
                          .forGetter(SummonPower::count),
                      Codec.floatRange(0.0F, 8.0F)
                          .optionalFieldOf("spread", 1.5F)
                          .forGetter(SummonPower::spread),
                      Codec.floatRange(0.0F, 4.0F)
                          .optionalFieldOf("rise", 1.0F)
                          .forGetter(SummonPower::rise),
                      Codec.intRange(1, 72000)
                          .optionalFieldOf("lifetime_ticks")
                          .forGetter(SummonPower::lifetimeTicks),
                      Codec.floatRange(1.0F, 1024.0F)
                          .optionalFieldOf("health")
                          .forGetter(SummonPower::health),
                      Codec.floatRange(0.0F, 1024.0F)
                          .optionalFieldOf("damage")
                          .forGetter(SummonPower::damage))
                  .apply(instance, SummonPower::new));

  public double offset(final double roll) {
    return (roll * 2.0 - 1.0) * spread;
  }
}
