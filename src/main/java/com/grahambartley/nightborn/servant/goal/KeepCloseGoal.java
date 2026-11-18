package com.grahambartley.nightborn.servant.goal;

import com.grahambartley.nightborn.servant.Servants;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.jetbrains.annotations.Nullable;

public class KeepCloseGoal extends Goal {

  static final int REPATH_TICKS = 10;

  private final PathAwareEntity servant;
  private final double startAt;
  private final double stopAt;
  @Nullable private LivingEntity master;
  private int repath;

  public KeepCloseGoal(final PathAwareEntity servant, final double startAt, final double stopAt) {
    this.servant = servant;
    this.startAt = startAt;
    this.stopAt = stopAt;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  public static boolean strays(final double distance, final double limit) {
    return distance > limit;
  }

  @Override
  public boolean canStart() {
    final LivingEntity here = Servants.masterHere(servant);
    if (servant.getTarget() != null || here == null || !strays(servant.distanceTo(here), startAt)) {
      return false;
    }
    master = here;
    return true;
  }

  @Override
  public boolean shouldContinue() {
    return master != null
        && master.isAlive()
        && servant.getTarget() == null
        && strays(servant.distanceTo(master), stopAt);
  }

  @Override
  public void start() {
    repath = 0;
  }

  @Override
  public void stop() {
    master = null;
    servant.getNavigation().stop();
  }

  @Override
  public void tick() {
    if (master == null) {
      return;
    }
    servant.getLookControl().lookAt(master, 10.0F, servant.getMaxLookPitchChange());
    if (--repath <= 0) {
      repath = getTickCount(REPATH_TICKS);
      servant.getNavigation().startMovingTo(master, 1.0);
    }
  }
}
