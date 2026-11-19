package com.grahambartley.nightborn.guild.raiding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

public record RaidOrders(UUID quarry, long until, Optional<BlockPos> place) {

  public static final Codec<RaidOrders> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.INT_STREAM_CODEC.fieldOf("quarry").forGetter(RaidOrders::quarry),
                      Codec.LONG.fieldOf("until").forGetter(RaidOrders::until),
                      BlockPos.CODEC.optionalFieldOf("place").forGetter(RaidOrders::place))
                  .apply(instance, RaidOrders::new));

  public boolean over(final long timeOfDay) {
    return timeOfDay >= until;
  }
}
