package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.core.NightTime;
import java.util.Collections;
import java.util.Set;
import java.util.function.IntPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class PlaceViews {

  static final int ROOM_LOOKED = 4;

  private PlaceViews() {}

  public static Situation.Place read(final MobEntity mob, final Set<Fact> needed) {
    if (Collections.disjoint(needed, Fact.PLACE)) {
      return Situation.Place.UNKNOWN;
    }
    final World world = mob.getWorld();
    return new Situation.Place(
        needed.contains(Fact.SPOT_DISTANCE)
            ? RouteEnd.distance(mob.getPos(), RouteEnd.of(mob))
            : Double.NaN,
        Collections.disjoint(needed, Fact.ROUTE) ? Route.UNKNOWN : RouteEnd.route(mob),
        needed.contains(Fact.HOME_DISTANCE)
            ? home(mob.hasPositionTarget(), mob.getPos(), mob.getPositionTarget())
            : Double.NaN,
        needed.contains(Fact.UNTIL_DAWN)
            ? NightTime.untilDawn(world.getDimension().hasFixedTime(), world.getTimeOfDay())
            : Double.NaN,
        needed.contains(Fact.LIGHT) ? world.getLightLevel(mob.getBlockPos()) : Double.NaN,
        needed.contains(Fact.ROOM_TO_FLY) ? roomAbove(mob) : Double.NaN);
  }

  public static double home(final boolean hasHome, final Vec3d at, final BlockPos home) {
    return hasHome ? at.distanceTo(Vec3d.ofBottomCenter(home)) : Double.NaN;
  }

  public static int room(final IntPredicate openAbove, final int looked) {
    int room = 0;
    while (room < looked && openAbove.test(room)) {
      room++;
    }
    return room;
  }

  private static int roomAbove(final MobEntity mob) {
    final World world = mob.getWorld();
    final BlockPos head =
        BlockPos.ofFloored(mob.getX(), Math.ceil(mob.getBoundingBox().maxY), mob.getZ());
    return room(
        step -> {
          final BlockPos at = head.up(step);
          return world.getBlockState(at).getCollisionShape(world, at).isEmpty();
        },
        ROOM_LOOKED);
  }
}
