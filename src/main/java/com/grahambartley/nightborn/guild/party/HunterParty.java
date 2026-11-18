package com.grahambartley.nightborn.guild.party;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

public record HunterParty(
    List<UUID> members,
    BlockPos area,
    Optional<HunterParty.Camp> camp,
    int nights,
    long campDay,
    long lastTriedDay,
    long lastPlayerNear,
    long lastRaidDay,
    Optional<BlockPos> guards) {

  public record Camp(BlockPos fire, List<BlockPos> blocks, List<BlockPos> beds) {
    public static final Codec<Camp> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        BlockPos.CODEC.fieldOf("fire").forGetter(Camp::fire),
                        BlockPos.CODEC.listOf().fieldOf("blocks").forGetter(Camp::blocks),
                        BlockPos.CODEC.listOf().fieldOf("beds").forGetter(Camp::beds))
                    .apply(instance, Camp::new));
  }

  public static final Codec<HunterParty> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.INT_STREAM_CODEC
                          .listOf()
                          .fieldOf("members")
                          .forGetter(HunterParty::members),
                      BlockPos.CODEC.fieldOf("area").forGetter(HunterParty::area),
                      Camp.CODEC.optionalFieldOf("camp").forGetter(HunterParty::camp),
                      Codec.INT.fieldOf("nights").forGetter(HunterParty::nights),
                      Codec.LONG.fieldOf("camp_day").forGetter(HunterParty::campDay),
                      Codec.LONG.fieldOf("last_tried_day").forGetter(HunterParty::lastTriedDay),
                      Codec.LONG.fieldOf("last_player_near").forGetter(HunterParty::lastPlayerNear),
                      Codec.LONG.fieldOf("last_raid_day").forGetter(HunterParty::lastRaidDay),
                      BlockPos.CODEC.optionalFieldOf("guards").forGetter(HunterParty::guards))
                  .apply(instance, HunterParty::new));

  public static HunterParty gathered(
      final List<UUID> members, final BlockPos area, final long now) {
    return new HunterParty(members, area, Optional.empty(), 0, 0L, -1L, now, -1L, Optional.empty());
  }

  public boolean pitched() {
    return camp.isPresent();
  }

  public HunterParty pitch(final Camp pitched, final int stay, final long day) {
    return new HunterParty(
        members,
        pitched.fire(),
        Optional.of(pitched),
        stay,
        day,
        day,
        lastPlayerNear,
        lastRaidDay,
        guards);
  }

  public HunterParty struck(final BlockPos nextArea) {
    return new HunterParty(
        members,
        nextArea,
        Optional.empty(),
        0,
        0L,
        lastTriedDay,
        lastPlayerNear,
        lastRaidDay,
        guards);
  }

  public HunterParty triedOn(final long day, final BlockPos nextArea) {
    return new HunterParty(
        members, nextArea, camp, nights, campDay, day, lastPlayerNear, lastRaidDay, guards);
  }

  public HunterParty seen(final long now) {
    return new HunterParty(
        members, area, camp, nights, campDay, lastTriedDay, now, lastRaidDay, guards);
  }

  public HunterParty raidRolled(final long day) {
    return new HunterParty(
        members, area, camp, nights, campDay, lastTriedDay, lastPlayerNear, day, guards);
  }

  public HunterParty guarding(final Optional<BlockPos> village) {
    return new HunterParty(
        members,
        village.orElse(area),
        camp,
        nights,
        campDay,
        lastTriedDay,
        lastPlayerNear,
        lastRaidDay,
        village);
  }

  public boolean guarding() {
    return guards.isPresent();
  }
}
