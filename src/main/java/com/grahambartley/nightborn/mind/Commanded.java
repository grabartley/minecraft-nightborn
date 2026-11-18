package com.grahambartley.nightborn.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

public record Commanded(
    UUID master, CommandOrder order, Optional<UUID> foe, Optional<BlockPos> door, long until) {

  public static final Codec<Commanded> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.INT_STREAM_CODEC.fieldOf("master").forGetter(Commanded::master),
                      CommandOrder.CODEC.fieldOf("order").forGetter(Commanded::order),
                      Uuids.INT_STREAM_CODEC.optionalFieldOf("foe").forGetter(Commanded::foe),
                      BlockPos.CODEC.optionalFieldOf("door").forGetter(Commanded::door),
                      Codec.LONG.fieldOf("until").forGetter(Commanded::until))
                  .apply(instance, Commanded::new));

  public boolean over(final long now) {
    return now >= until;
  }

  public Commanded following() {
    return new Commanded(master, CommandOrder.FOLLOW, Optional.empty(), Optional.empty(), until);
  }
}
