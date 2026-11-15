package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.IntProvider;

public record Household(
    RegistryEntry<EntityType<?>> resident, IntProvider residents, Map<Identifier, Integer> stages) {

  public static final int LARGEST_HOUSEHOLD = 16;
  public static final int HEAVIEST_STAGE = 1000;

  public static final Codec<Household> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Registries.ENTITY_TYPE
                          .getEntryCodec()
                          .fieldOf("resident")
                          .forGetter(Household::resident),
                      IntProvider.createValidatingCodec(0, LARGEST_HOUSEHOLD)
                          .fieldOf("residents")
                          .forGetter(Household::residents),
                      Codec.unboundedMap(Identifier.CODEC, Codec.intRange(0, HEAVIEST_STAGE))
                          .optionalFieldOf("stages", Map.of())
                          .forGetter(Household::stages))
                  .apply(instance, Household::new));

  public Household {
    stages = Map.copyOf(stages);
  }
}
