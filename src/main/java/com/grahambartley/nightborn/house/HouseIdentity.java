package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.house.crest.Crest;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record HouseIdentity(Optional<String> name, Optional<Crest> crest) {

  public static final HouseIdentity NONE = new HouseIdentity(Optional.empty(), Optional.empty());

  public static final Codec<HouseIdentity> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.optionalFieldOf("name").forGetter(HouseIdentity::name),
                      Crest.CODEC.optionalFieldOf("crest").forGetter(HouseIdentity::crest))
                  .apply(instance, HouseIdentity::new));
}
