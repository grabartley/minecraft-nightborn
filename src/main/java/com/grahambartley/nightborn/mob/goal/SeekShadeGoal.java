package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedRules;
import com.grahambartley.nightborn.mob.ShadeRules;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SeekShadeGoal extends Goal {

  static final int SEARCH_RADIUS = 12;
  static final int SEARCH_HEIGHT = 4;
  static final int ROUTE_TRIES = 4;
  static final int RETRY_TICKS = 20;

  private static final List<Vec3i> NEAREST_FIRST =
      ShadeSearch.nearestFirst(SEARCH_RADIUS, SEARCH_HEIGHT);

  private final PathAwareEntity mob;
  private final double speed;
  @Nullable private Path route;
  private int nextSearchAge;

  public SeekShadeGoal(final PathAwareEntity mob, final double speed) {
    this.mob = mob;
    this.speed = speed;
    setControls(EnumSet.of(Control.MOVE));
  }

  @Override
  public boolean canStart() {
    if (!inDanger() || mob.age < nextSearchAge) {
      return false;
    }
    route = routeToShade().orElse(null);
    if (route == null) {
      nextSearchAge = mob.age + RETRY_TICKS;
      return false;
    }
    return true;
  }

  @Override
  public boolean shouldContinue() {
    return inDanger() && !mob.getNavigation().isIdle();
  }

  @Override
  public void start() {
    mob.setTarget(null);
    mob.getNavigation().startMovingAlong(route, speed);
  }

  @Override
  public void stop() {
    route = null;
    mob.getNavigation().stop();
  }

  private boolean inDanger() {
    return !DownedRules.helpless(Downed.state(mob)) && ShadeRules.endangered(mob);
  }

  private Optional<Path> routeToShade() {
    final World world = mob.getWorld();
    final BlockPos origin = mob.getBlockPos();
    return ShadeSearch.nearestReachable(
        NEAREST_FIRST,
        offset -> standsInShade(world, origin.add(offset)),
        offset ->
            Optional.ofNullable(mob.getNavigation().findPathTo(origin.add(offset), 0))
                .filter(Path::reachesTarget),
        ROUTE_TRIES);
  }

  private static boolean standsInShade(final World world, final BlockPos feet) {
    return world.isAir(feet)
        && world.isAir(feet.up())
        && world.getBlockState(feet.down()).isSolidBlock(world, feet.down())
        && !world.isSkyVisible(feet.up());
  }
}
