package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.blood.GiftModifier;
import com.grahambartley.nightborn.faction.Faction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public record Accessory(Optional<Faction> worksFor, List<GiftModifier> gifts) {

  public static final Codec<Accessory> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Faction.CODEC.optionalFieldOf("works_for").forGetter(Accessory::worksFor),
                      GiftModifier.CODEC
                          .listOf()
                          .optionalFieldOf("gifts", List.of())
                          .forGetter(Accessory::gifts))
                  .apply(instance, Accessory::new));

  public boolean worksFor(final Faction wearer) {
    return this.worksFor.map(wearer::equals).orElse(true);
  }
}
