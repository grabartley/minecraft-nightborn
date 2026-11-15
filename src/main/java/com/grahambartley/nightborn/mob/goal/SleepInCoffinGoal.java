package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.HomeCoffins;
import com.grahambartley.nightborn.mob.home.NightRoutine;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SleepInCoffinGoal extends Goal {

  static final double LIE_DOWN_REACH = 2.0;
  static final int REPATH_TICKS = 10;
  static final double SPEED = 1.0;

  private final VampireEntity vampire;
  @Nullable private BlockPos coffin;
  private int repath;

  public SleepInCoffinGoal(final VampireEntity vampire) {
    this.vampire = vampire;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
  }

  @Override
  public boolean canStart() {
    if (vampire.isSleeping()
        || !readyForBed()
        || !(vampire.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    final BlockPos known = vampire.homeLife().coffin().orElse(null);
    final boolean loaded = known != null && HomeCoffins.loaded(world, known);
    if (!NightRoutine.walksToCoffin(
        known != null,
        known == null ? 0.0 : known.getSquaredDistance(vampire.getPos()),
        ServerConfigService.current().mobs().vampireHomeRadius(),
        loaded,
        loaded && HomeCoffins.free(world, known))) {
      return false;
    }
    coffin = known;
    return true;
  }

  @Override
  public boolean shouldContinue() {
    return vampire.isSleeping()
        || (coffin != null
            && readyForBed()
            && vampire.getWorld() instanceof ServerWorld world
            && HomeCoffins.free(world, coffin));
  }

  @Override
  public boolean canStop() {
    return !vampire.isSleeping();
  }

  @Override
  public void start() {
    repath = 0;
  }

  @Override
  public void tick() {
    if (coffin == null || vampire.isSleeping()) {
      return;
    }
    if (coffin.isWithinDistance(vampire.getPos(), LIE_DOWN_REACH)) {
      vampire.getNavigation().stop();
      vampire.sleep(coffin);
      return;
    }
    if (--repath <= 0) {
      repath = REPATH_TICKS;
      vampire
          .getNavigation()
          .startMovingTo(coffin.getX() + 0.5, coffin.getY(), coffin.getZ() + 0.5, SPEED);
    }
  }

  @Override
  public void stop() {
    coffin = null;
    vampire.getNavigation().stop();
  }

  private boolean readyForBed() {
    return NightRoutine.readyForBed(
        vampire.homeLife().bedtime(vampire),
        vampire.getTarget() != null,
        Downed.state(vampire) != null);
  }
}
