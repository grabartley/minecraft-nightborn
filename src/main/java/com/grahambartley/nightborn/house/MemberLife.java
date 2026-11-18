package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.HouseholdTie;
import com.grahambartley.nightborn.mob.home.NightRoutine;
import com.grahambartley.nightborn.servant.FollowRules;
import com.grahambartley.nightborn.servant.Stance;
import com.grahambartley.nightborn.structure.HouseholdRoll;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;

public final class MemberLife {

  static final int REVIEW_TICKS = 20;

  private MemberLife() {}

  public static void tick(final VampireEntity member) {
    if (!(member.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final HouseholdTie tie = member.householdTie();
    if (member.age % REVIEW_TICKS == 0) {
      review(world, member, tie);
    }
    tie.stance()
        .ifPresent(
            stance ->
                member.setPositionTarget(
                    tie.post().orElse(BlockPos.ORIGIN), FollowRules.leash(stance)));
  }

  public static boolean guards(final VampireEntity member, final LivingEntity target) {
    final MemberStance stance = new MemberStance(member);
    return stance.holds(Stance.GUARD)
        && stance.guardsAgainst(target)
        && FollowRules.guards(
            Stance.GUARD, Math.sqrt(target.squaredDistanceTo(Vec3d.ofBottomCenter(stance.post()))));
  }

  public static long tonight(final VampireEntity member) {
    return NightRoutine.night(member.getWorld().getTimeOfDay());
  }

  public static boolean onErrand(final VampireEntity member) {
    return member.householdTie().onErrand(tonight(member));
  }

  public static void fed(final VampireEntity member, final LivingEntity prey, final boolean out) {
    final HouseholdTie tie = member.householdTie();
    if (!(member.getWorld() instanceof ServerWorld world)
        || tie.lord().isEmpty()
        || !(out || onErrand(member))) {
      return;
    }
    tie.fed(
        tonight(member),
        BloodQuality.of(prey.getType()),
        houseOf(world.getServer(), member).flatMap(House::store).isPresent());
  }

  public static void sendOut(final VampireEntity member, final boolean providing) {
    final long night =
        NightRoutine.comingNight(
            member.getWorld().getDimension().hasFixedTime(), member.getWorld().getTimeOfDay());
    member.householdTie().sendOut(night, providing);
    member.homeLife().sendOut(night);
  }

  public static void join(final ServerWorld world, final VampireEntity member, final House house) {
    leaveOldHousehold(world, member);
    member.householdTie().join(house.lord());
    final Houses houses = Houses.get(world.getServer());
    houses.put(house.withNpc(member.getUuid()));
    houses.ledBy(house.lord()).ifPresent(joined -> settle(world, member, joined));
  }

  public static void goWild(final ServerWorld world, final VampireEntity member) {
    member
        .householdTie()
        .lord()
        .flatMap(Houses.get(world.getServer())::ledBy)
        .ifPresent(house -> Houses.get(world.getServer()).put(house.without(member.getUuid())));
    member.householdTie().leave();
    member.homeLife().release(world, member);
    member.homeLife().wander();
    member.setPositionTarget(BlockPos.ORIGIN, NightRoutine.UNLEASHED);
  }

  public static void ended(final ServerWorld world, final VampireEntity member) {
    leaveOldHousehold(world, member);
  }

  public static Optional<House> houseOf(final MinecraftServer server, final VampireEntity member) {
    final UUID self = member.getUuid();
    return member
        .householdTie()
        .lord()
        .flatMap(Houses.get(server)::ledBy)
        .filter(house -> house.npcs().contains(self));
  }

  private static void leaveOldHousehold(final ServerWorld world, final VampireEntity member) {
    final MinecraftServer server = world.getServer();
    final UUID self = member.getUuid();
    member
        .householdTie()
        .lord()
        .flatMap(Houses.get(server)::ledBy)
        .ifPresent(house -> Houses.get(server).put(house.without(self)));
    if (member.householdTie().lord().isEmpty()) {
      member
          .homeLife()
          .home()
          .ifPresent(
              home ->
                  HouseholdRoll.get(server)
                      .forget(GlobalPos.create(world.getRegistryKey(), home), self));
    }
    member.homeLife().release(world, member);
  }

  private static void review(
      final ServerWorld world, final VampireEntity member, final HouseholdTie tie) {
    if (tie.lord().isEmpty()) {
      enlist(world, member, tie);
      return;
    }
    final Optional<House> house = houseOf(world.getServer(), member);
    if (house.isEmpty()) {
      goWild(world, member);
      return;
    }
    settle(world, member, house.get());
    final long night = tonight(member);
    if (tie.provider()
        && !tie.sentOutOn(night)
        && NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay())) {
      sendOut(member, true);
    }
  }

  private static void settle(
      final ServerWorld world, final VampireEntity member, final House house) {
    HousePlace.of(world.getServer(), house)
        .filter(place -> place.dimension().equals(world.getRegistryKey()))
        .filter(place -> member.homeLife().home().filter(place.pos()::equals).isEmpty())
        .ifPresent(place -> member.homeLife().settle(place.pos()));
  }

  private static void enlist(
      final ServerWorld world, final VampireEntity member, final HouseholdTie tie) {
    member
        .homeLife()
        .home()
        .ifPresent(
            home -> {
              final GlobalPos at = GlobalPos.create(world.getRegistryKey(), home);
              final HouseholdRoll roll = HouseholdRoll.get(world.getServer());
              roll.enlist(at, member.getUuid());
              if (tie.head()) {
                member
                    .homeLife()
                    .coffin()
                    .ifPresent(
                        coffin ->
                            roll.headSleepsAt(
                                at, GlobalPos.create(world.getRegistryKey(), coffin)));
              }
            });
  }
}
