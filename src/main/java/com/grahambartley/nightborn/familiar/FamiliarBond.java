package com.grahambartley.nightborn.familiar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record FamiliarBond(UUID familiar) {

  public static final Codec<FamiliarBond> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.INT_STREAM_CODEC.fieldOf("familiar").forGetter(FamiliarBond::familiar))
                  .apply(instance, FamiliarBond::new));
}
