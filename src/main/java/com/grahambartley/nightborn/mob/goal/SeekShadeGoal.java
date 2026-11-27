package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedRules;
import com.grahambartley.nightborn.mob.ShadeRules;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
  static final int BATCH_SIZE = 8;
  static final int BATCHES = 4;
  static final int RETRY_TICKS = 20;

  private static final List<Vec3i> NEAREST_FIRST =
      ShadeSearch.nearestFirst(SEARCH_RADIUS, SEARCH_HEIGHT);

  private final PathAwareEntity mob;
  private final double speed;
  private final Set<Vec3i> ruledOut = new HashSet<>();
  @Nullable private Path route;
  @Nullable private BlockPos searchedFrom;
  private int nextSearchAge;

  public SeekShadeGoal(final PathAwareEntity mob, final double speed) {
    this.mob = mob;
    this.speed = speed;
    setControls(EnumSet.of(Control.MOVE));
  }

  @Override
  public boolean canStart() {
    if (!inDanger() || !standing() || mob.age < nextSearchAge) {
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

  private boolean standing() {
    return mob.isOnGround() || mob.isInFluid() || mob.hasVehicle();
  }

  private boolean inDanger() {
    return !DownedRules.helpless(Downed.state(mob)) && ShadeRules.endangered(mob);
  }

  private Optional<Path> routeToShade() {
    final World world = mob.getWorld();
    final BlockPos origin = mob.getBlockPos();
    if (!origin.equals(searchedFrom)) {
      ruledOut.clear();
      searchedFrom = origin;
    }
    return ShadeSearch.nearestReachable(
        NEAREST_FIRST,
        offset -> standsInShade(world, origin.add(offset)),
        ruledOut,
        spots ->
            Optional.ofNullable(
                    mob.getNavigation()
                        .findPathTo(spots.stream().map(origin::add).collect(Collectors.toSet()), 0))
                .filter(Path::reachesTarget),
        BATCH_SIZE,
        BATCHES);
  }

  private static boolean standsInShade(final World world, final BlockPos feet) {
    return world.isAir(feet)
        && world.isAir(feet.up())
        && world.getBlockState(feet.down()).isSolidBlock(world, feet.down())
        && ShadeRules.shadedAt(world, feet);
  }
}
