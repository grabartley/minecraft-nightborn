package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.trap.Baits;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FollowBaitGoal extends Goal {

  static final double SPEED = 1.1;
  static final double CLOSE = 1.5;

  private final VampireEntity vampire;
  @Nullable private BlockPos bait;

  public FollowBaitGoal(final VampireEntity vampire) {
    this.vampire = vampire;
    setControls(EnumSet.of(Control.MOVE));
  }

  public static boolean drawn(final boolean hungry, final boolean fighting, final boolean lured) {
    return hungry && !fighting && lured;
  }

  public static boolean arrived(final double squaredDistance) {
    return squaredDistance <= CLOSE * CLOSE;
  }

  @Override
  public boolean canStart() {
    if (!drawn(vampire.hungry(), vampire.getTarget() != null, PowerReach.marks(vampire))) {
      return false;
    }
    bait = Baits.nearest(vampire.getWorld(), vampire.getPos()).orElse(null);
    return bait != null && !arrived(vampire.squaredDistanceTo(bait.toBottomCenterPos()));
  }

  @Override
  public void start() {
    if (bait != null) {
      vampire
          .getNavigation()
          .startMovingTo(bait.getX() + 0.5, bait.getY(), bait.getZ() + 0.5, SPEED);
    }
  }

  @Override
  public boolean shouldContinue() {
    return bait != null
        && drawn(vampire.hungry(), vampire.getTarget() != null, true)
        && Baits.lasts(vampire.getWorld(), bait)
        && !arrived(vampire.squaredDistanceTo(bait.toBottomCenterPos()))
        && !vampire.getNavigation().isIdle();
  }

  @Override
  public void stop() {
    bait = null;
    vampire.getNavigation().stop();
  }
}
