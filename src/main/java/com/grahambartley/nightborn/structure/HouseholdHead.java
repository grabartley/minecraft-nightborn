package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.mob.stage.StageOdds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public record HouseholdHead(
    RegistryEntry<EntityType<?>> resident, Map<Identifier, Integer> stages, HeadSeat seat) {

  public static final Codec<HouseholdHead> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Registries.ENTITY_TYPE
                          .getEntryCodec()
                          .fieldOf("resident")
                          .forGetter(HouseholdHead::resident),
                      Codec.unboundedMap(Identifier.CODEC, Codec.intRange(0, StageOdds.HEAVIEST))
                          .optionalFieldOf("stages", Map.of())
                          .forGetter(HouseholdHead::stages),
                      HeadSeat.CODEC
                          .optionalFieldOf("seat", HeadSeat.LOWEST)
                          .forGetter(HouseholdHead::seat))
                  .apply(instance, HouseholdHead::new));

  public HouseholdHead {
    stages = Map.copyOf(stages);
  }
}
