package com.grahambartley.nightborn.guild.party;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

public record HunterParty(
    List<UUID> members,
    BlockPos area,
    Optional<HunterParty.Camp> camp,
    CampStay stay,
    long lastTriedDay,
    long lastPlayerNear,
    long lastRaidDay,
    Optional<BlockPos> guards,
    Optional<BlockPos> lastCamp) {

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
      RecordCodecBuilder.<HunterParty>create(
              instance ->
                  instance
                      .group(
                          Uuids.INT_STREAM_CODEC
                              .listOf()
                              .fieldOf("members")
                              .forGetter(HunterParty::members),
                          BlockPos.CODEC.fieldOf("area").forGetter(HunterParty::area),
                          Camp.CODEC.optionalFieldOf("camp").forGetter(HunterParty::camp),
                          CampStay.MAP_CODEC.forGetter(HunterParty::stay),
                          Codec.LONG.fieldOf("last_tried_day").forGetter(HunterParty::lastTriedDay),
                          Codec.LONG
                              .fieldOf("last_player_near")
                              .forGetter(HunterParty::lastPlayerNear),
                          Codec.LONG.fieldOf("last_raid_day").forGetter(HunterParty::lastRaidDay),
                          BlockPos.CODEC.optionalFieldOf("guards").forGetter(HunterParty::guards),
                          BlockPos.CODEC
                              .optionalFieldOf("last_camp")
                              .forGetter(HunterParty::lastCamp))
                      .apply(instance, HunterParty::new))
          .xmap(HunterParty::upgraded, Function.<HunterParty>identity());

  public static HunterParty gathered(
      final List<UUID> members, final BlockPos area, final long now) {
    return new HunterParty(
        members,
        area,
        Optional.empty(),
        CampStay.NONE,
        -1L,
        now,
        -1L,
        Optional.empty(),
        Optional.empty());
  }

  public boolean pitched() {
    return camp.isPresent();
  }

  public HunterParty pitch(final Camp pitched, final int nights, final long day, final long now) {
    return new HunterParty(
        members,
        pitched.fire(),
        Optional.of(pitched),
        guards
            .map(bell -> CampStay.guarding(bell, day, now))
            .orElseGet(() -> new CampStay(nights, day, now)),
        day,
        lastPlayerNear,
        lastRaidDay,
        guards,
        lastCamp);
  }

  public HunterParty struck(final BlockPos nextArea) {
    return new HunterParty(
        members,
        nextArea,
        Optional.empty(),
        CampStay.NONE,
        lastTriedDay,
        lastPlayerNear,
        lastRaidDay,
        guards,
        camp.map(Camp::fire).or(() -> lastCamp));
  }

  public HunterParty upgraded() {
    if (!pitched() || stay.forGuard() || guards.isEmpty()) {
      return this;
    }
    return new HunterParty(
        members,
        area,
        camp,
        CampStay.guarding(guards.get(), stay.campDay(), stay.pitchedAt()),
        lastTriedDay,
        lastPlayerNear,
        lastRaidDay,
        guards,
        lastCamp);
  }

  public HunterParty relieved(final long now) {
    return new HunterParty(
        members,
        area,
        camp,
        stay.relieve(now),
        lastTriedDay,
        lastPlayerNear,
        lastRaidDay,
        Optional.empty(),
        lastCamp);
  }

  public HunterParty heading(final BlockPos nextArea) {
    return new HunterParty(
        members, nextArea, camp, stay, lastTriedDay, lastPlayerNear, lastRaidDay, guards, lastCamp);
  }

  public HunterParty triedOn(final long day, final BlockPos nextArea) {
    return new HunterParty(
        members, nextArea, camp, stay, day, lastPlayerNear, lastRaidDay, guards, lastCamp);
  }

  public HunterParty seen(final long now) {
    return new HunterParty(
        members, area, camp, stay, lastTriedDay, now, lastRaidDay, guards, lastCamp);
  }

  public HunterParty raidRolled(final long day) {
    return new HunterParty(
        members, area, camp, stay, lastTriedDay, lastPlayerNear, day, guards, lastCamp);
  }

  public HunterParty guarding(final Optional<BlockPos> village) {
    return new HunterParty(
        members,
        village.orElse(area),
        camp,
        stay,
        lastTriedDay,
        lastPlayerNear,
        lastRaidDay,
        village,
        lastCamp);
  }

  public boolean guarding() {
    return guards.isPresent();
  }
}
