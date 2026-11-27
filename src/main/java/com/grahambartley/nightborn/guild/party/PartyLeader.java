package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.config.PartySettings;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.village.VillageGuard;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public final class PartyLeader {

  public static final int TICKS = 20;
  public static final int TICKS_PER_MINUTE = 1200;
  public static final double PLAYER_NEAR = 128.0;
  public static final int KEEP_CLOSE = 8;
  public static final int AREA_REACH = 16;
  public static final int WATCH_REACH = 12;
  public static final int DAY_REACH = 24;
  public static final int MOVE_MIN = 48;
  public static final int MOVE_MAX = 80;
  private static final float TRAVEL_SWAY = MathHelper.HALF_PI / 2;

  private PartyLeader() {}

  @Nullable
  public static HunterParty tick(
      final ServerWorld world, final GuildMasterEntity master, final HunterParty start) {
    final PartySettings settings = ServerConfigService.current().parties();
    final long now = world.getTime();
    HunterParty party =
        world.getClosestPlayer(master, PLAYER_NEAR) != null ? start.seen(now) : start;
    if (VillageGuard.relieves(party.guards(), bell -> VillageGuard.stillGuarded(world, bell))) {
      party = party.relieved(now);
    }
    if (party.pitched()) {
      PitchedCamps.of(world).kept(master.getUuid(), party.stay());
    }
    final List<HunterEntity> members = members(world, master.getUuid(), party);
    if (PartySchedule.leaves(
        party.pitched(),
        party.guarding(),
        now,
        party.lastPlayerNear(),
        (long) settings.partyLeaveMinutes() * TICKS_PER_MINUTE)) {
      members.forEach(HunterEntity::discard);
      master.discard();
      return null;
    }
    if (PassingRaid.raids(world, master, party, members)) {
      return party;
    }
    final long time = world.getTimeOfDay();
    final long day = PartySchedule.day(time);
    final Random random = master.getRandom();
    party = raid(world, party, settings, time, day, random);
    switch (PartySchedule.step(
        time, party.pitched(), party.stay().over(time, now), party.lastTriedDay())) {
      case PITCH -> party = pitch(world, master, party, settings, day, random);
      case SLEEP -> settle(world, party, members, settings);
      case WAKE -> roam(party, members);
      case STRIKE -> party = strike(world, master, party, members, random);
      case NONE -> party = follow(master, party, members, time, random);
    }
    return party;
  }

  private static List<HunterEntity> members(
      final ServerWorld world, final UUID leader, final HunterParty party) {
    return party.members().stream()
        .map(world::getEntity)
        .filter(HunterEntity.class::isInstance)
        .map(HunterEntity.class::cast)
        .filter(HunterEntity::isAlive)
        .filter(member -> PartyMember.follows(member.partyLeader(), leader))
        .toList();
  }

  private static HunterParty pitch(
      final ServerWorld world,
      final GuildMasterEntity master,
      final HunterParty party,
      final PartySettings settings,
      final long day,
      final Random random) {
    final Optional<CampLayout> layout = PartyData.layout(world, random);
    final Optional<BlockPos> site =
        layout.flatMap(chosen -> site(world, master.getBlockPos(), chosen, party.lastCamp()));
    if (site.isEmpty()) {
      return party.triedOn(
          day, party.guards().orElseGet(() -> onward(master.getBlockPos(), random)));
    }
    final HunterParty.Camp camp = PartyCamp.pitch(world, site.get(), layout.get());
    master.setPositionTarget(camp.fire(), KEEP_CLOSE);
    final HunterParty pitched =
        party.pitch(
            camp,
            PartyRoll.nights(
                random::nextInt, settings.partyExtraNightChance(), settings.partyMaxNights()),
            day,
            world.getTime());
    PitchedCamps.of(world).pitched(master.getUuid(), camp, pitched.stay());
    return pitched;
  }

  private static Optional<BlockPos> site(
      final ServerWorld world,
      final BlockPos near,
      final CampLayout layout,
      final Optional<BlockPos> lastCamp) {
    return CampSite.choose(
        () -> PartyCamp.site(world, near, layout.bounds(), lastCamp),
        () -> lastCamp.flatMap(fire -> PartyCamp.around(world, near, fire, layout)),
        () -> PartyCamp.site(world, near, layout.bounds(), Optional.empty()));
  }

  private static void settle(
      final ServerWorld world,
      final HunterParty party,
      final List<HunterEntity> members,
      final PartySettings settings) {
    final HunterParty.Camp camp = party.camp().orElseThrow();
    if (!CampRaid.over(CampBats.raidersNear(world, camp.fire()))) {
      final List<HunterEntity> sleeping =
          members.stream().filter(HunterEntity::isSleeping).toList();
      CampRaid.wakers(
              sleeping.stream()
                  .map(member -> Math.sqrt(member.getBlockPos().getSquaredDistance(camp.fire())))
                  .toList())
          .forEach(i -> wake(sleeping.get(i), camp.fire(), WATCH_REACH));
      members.stream()
          .filter(member -> !member.isSleeping())
          .forEach(member -> member.setPositionTarget(camp.fire(), WATCH_REACH));
      return;
    }
    final int sleepers =
        PartyRoll.sleepers(members.size(), camp.beds().size(), settings.partySleepers());
    for (int i = 0; i < members.size(); i++) {
      final HunterEntity member = members.get(i);
      if (i < sleepers) {
        if (!member.isSleeping()) {
          member.getNavigation().stop();
          member.sleep(camp.beds().get(i));
        }
      } else {
        wake(member, camp.fire(), WATCH_REACH);
      }
    }
  }

  private static void roam(final HunterParty party, final List<HunterEntity> members) {
    final BlockPos fire = party.camp().orElseThrow().fire();
    members.forEach(member -> wake(member, fire, DAY_REACH));
  }

  private static HunterParty strike(
      final ServerWorld world,
      final GuildMasterEntity master,
      final HunterParty party,
      final List<HunterEntity> members,
      final Random random) {
    final HunterParty.Camp camp = party.camp().orElseThrow();
    members.forEach(member -> wake(member, camp.fire(), KEEP_CLOSE));
    PartyCamp.strike(world, camp);
    PitchedCamps.of(world).struck(master.getUuid());
    final BlockPos next = onward(camp.fire(), random);
    master.setPositionTarget(next, AREA_REACH);
    return party.struck(next);
  }

  private static HunterParty follow(
      final GuildMasterEntity master,
      final HunterParty start,
      final List<HunterEntity> members,
      final long time,
      final Random random) {
    final HunterParty party =
        travels(
                start.guarding(),
                start.lastCamp().isPresent(),
                PartySchedule.dusk(time),
                master.getBlockPos(),
                start.area())
            ? start.heading(beyond(start.lastCamp().orElseThrow(), start.area(), random))
            : start;
    master.setPositionTarget(party.area(), AREA_REACH);
    members.forEach(member -> wake(member, master.getBlockPos(), KEEP_CLOSE));
    return party;
  }

  private static HunterParty raid(
      final ServerWorld world,
      final HunterParty party,
      final PartySettings settings,
      final long time,
      final long day,
      final Random random) {
    if (!CampRaid.due(PartySchedule.night(time), party.pitched(), party.lastRaidDay() == day)) {
      return party;
    }
    if (PartyRoll.succeeds(random::nextInt, settings.campBatChance())) {
      CampBats.raid(world, party.camp().orElseThrow().fire(), settings.campBatCount(), random);
    }
    return party.raidRolled(day);
  }

  private static void wake(final PathAwareEntity member, final BlockPos home, final int reach) {
    if (member.isSleeping()) {
      member.wakeUp();
    }
    member.setPositionTarget(home, reach);
  }

  static boolean travels(
      final boolean guarding,
      final boolean leftACamp,
      final boolean dusk,
      final BlockPos at,
      final BlockPos area) {
    return !guarding && leftACamp && !dusk && CampSite.inReach(at, area, AREA_REACH);
  }

  static BlockPos onward(final BlockPos from, final Random random) {
    return step(from, random.nextFloat() * MathHelper.TAU, random);
  }

  static BlockPos beyond(final BlockPos lastCamp, final BlockPos from, final Random random) {
    final float away =
        (float) MathHelper.atan2(from.getZ() - lastCamp.getZ(), from.getX() - lastCamp.getX());
    return step(from, away + (random.nextFloat() * 2 - 1) * TRAVEL_SWAY, random);
  }

  private static BlockPos step(final BlockPos from, final float angle, final Random random) {
    final int distance = random.nextBetween(MOVE_MIN, MOVE_MAX);
    return Objects.requireNonNull(from)
        .add(
            Math.round(MathHelper.cos(angle) * distance),
            0,
            Math.round(MathHelper.sin(angle) * distance));
  }
}
