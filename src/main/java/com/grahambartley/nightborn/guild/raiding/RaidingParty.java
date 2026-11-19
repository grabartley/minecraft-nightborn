package com.grahambartley.nightborn.guild.raiding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.GlobalPos;

public record RaidingParty(
    UUID quarry,
    List<UUID> members,
    int size,
    long until,
    Optional<GlobalPos> place,
    Optional<UUID> leader) {

  public static final Codec<RaidingParty> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.INT_STREAM_CODEC.fieldOf("quarry").forGetter(RaidingParty::quarry),
                      Uuids.INT_STREAM_CODEC
                          .listOf()
                          .fieldOf("members")
                          .forGetter(RaidingParty::members),
                      Codec.INT.fieldOf("size").forGetter(RaidingParty::size),
                      Codec.LONG.fieldOf("until").forGetter(RaidingParty::until),
                      GlobalPos.CODEC.optionalFieldOf("place").forGetter(RaidingParty::place),
                      Uuids.INT_STREAM_CODEC
                          .optionalFieldOf("leader")
                          .forGetter(RaidingParty::leader))
                  .apply(instance, RaidingParty::new));

  public RaidingParty {
    members = List.copyOf(members);
  }

  public RaidingParty without(final UUID member) {
    return new RaidingParty(
        quarry,
        members.stream().filter(id -> !id.equals(member)).toList(),
        size,
        until,
        place,
        leader);
  }

  public boolean passing() {
    return leader.isPresent();
  }

  public RaidOrders orders() {
    return new RaidOrders(quarry, until, place.map(GlobalPos::pos));
  }
}
