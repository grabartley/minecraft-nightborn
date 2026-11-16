package com.grahambartley.nightborn.familiar.goal;

import com.grahambartley.nightborn.familiar.FamiliarClock;
import com.grahambartley.nightborn.familiar.HomeRules;
import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class StayHomeGoal extends Goal {

  static final double SPEED = 0.7;

  private final HumanFamiliarEntity familiar;

  public StayHomeGoal(final HumanFamiliarEntity familiar) {
    this.familiar = familiar;
    setControls(EnumSet.of(Control.MOVE));
  }

  @Override
  public boolean canStart() {
    return familiar
        .home()
        .filter(home -> !familiar.getBlockPos().isWithinDistance(home, range()))
        .isPresent();
  }

  @Override
  public boolean shouldContinue() {
    return !familiar.getNavigation().isIdle() && canStart();
  }

  @Override
  public void start() {
    final BlockPos home = familiar.home().orElseThrow();
    familiar
        .getNavigation()
        .startMovingTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, SPEED);
  }

  @Override
  public void stop() {
    familiar.getNavigation().stop();
  }

  private int range() {
    return HomeRules.range(
        FamiliarClock.isWorkingHours(familiar.getWorld()),
        ServerConfigService.current().familiars().guardRange());
  }
}
