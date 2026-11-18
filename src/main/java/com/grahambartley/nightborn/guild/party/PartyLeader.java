package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.config.PartySettings;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.village.VillageGuard;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

  private PartyLeader() {}

  @Nullable
  public static HunterParty tick(
      final ServerWorld world, final GuildMasterEntity master, final HunterParty start) {
    final PartySettings settings = ServerConfigService.current().parties();
    final long now = world.getTime();
    HunterParty party =
        world.getClosestPlayer(master, PLAYER_NEAR) != null ? start.seen(now) : start;
    if (party.guards().filter(bell -> !VillageGuard.stillGuarded(world, bell)).isPresent()) {
      party = party.guarding(Optional.empty());
    }
    final List<HunterEntity> members = members(world, party);
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
    final long time = world.getTimeOfDay();
    final long day = PartySchedule.day(time);
    final Random random = master.getRandom();
    party = raid(world, party, settings, time, day, random);
    switch (PartySchedule.step(
        time,
        party.pitched(),
        party.campDay(),
        party.nights(),
        party.lastTriedDay(),
        party.guarding())) {
      case PITCH -> party = pitch(world, master, party, settings, day, random);
      case SLEEP -> settle(world, party, members, settings);
      case WAKE -> roam(party, members);
      case STRIKE -> party = strike(world, master, party, members, random);
      case NONE -> follow(master, party, members);
    }
    return party;
  }

  private static List<HunterEntity> members(final ServerWorld world, final HunterParty party) {
    return party.members().stream()
        .map(world::getEntity)
        .filter(HunterEntity.class::isInstance)
        .map(HunterEntity.class::cast)
        .filter(HunterEntity::isAlive)
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
        layout.flatMap(chosen -> PartyCamp.site(world, master.getBlockPos(), chosen.bounds()));
    if (site.isEmpty()) {
      return party.triedOn(
          day, party.guards().orElseGet(() -> onward(master.getBlockPos(), random)));
    }
    final HunterParty.Camp camp = PartyCamp.pitch(world, site.get(), layout.get());
    master.setPositionTarget(camp.fire(), KEEP_CLOSE);
    return party.pitch(
        camp,
        PartyRoll.nights(
            random::nextInt, settings.partyExtraNightChance(), settings.partyMaxNights()),
        day);
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
    final BlockPos next = onward(camp.fire(), random);
    master.setPositionTarget(next, AREA_REACH);
    return party.struck(next);
  }

  private static void follow(
      final GuildMasterEntity master, final HunterParty party, final List<HunterEntity> members) {
    master.setPositionTarget(party.area(), AREA_REACH);
    members.forEach(member -> wake(member, master.getBlockPos(), KEEP_CLOSE));
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

  static BlockPos onward(final BlockPos from, final Random random) {
    final float angle = random.nextFloat() * MathHelper.TAU;
    final int distance = random.nextBetween(MOVE_MIN, MOVE_MAX);
    return Objects.requireNonNull(from)
        .add(
            Math.round(MathHelper.cos(angle) * distance),
            0,
            Math.round(MathHelper.sin(angle) * distance));
  }
}
