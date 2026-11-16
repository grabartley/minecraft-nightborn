package com.grahambartley.nightborn.thrall.goal;

import com.grahambartley.nightborn.thrall.FollowRules;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

public class ThrallFollowGoal extends Goal {

  static final int REPATH_TICKS = 10;
  static final double SPEED = 1.0;

  private final ThrallEntity thrall;
  @Nullable private LivingEntity owner;
  private int repath;

  public ThrallFollowGoal(final ThrallEntity thrall) {
    this.thrall = thrall;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    final LivingEntity here = thrall.ownerHere();
    if (thrall.getTarget() != null
        || !FollowRules.follows(
            thrall.mode(), here != null, here == null ? 0.0 : thrall.distanceTo(here))) {
      return false;
    }
    owner = here;
    return true;
  }

  @Override
  public boolean shouldContinue() {
    return owner != null
        && owner.isAlive()
        && thrall.getTarget() == null
        && FollowRules.keepsFollowing(thrall.mode(), true, thrall.distanceTo(owner));
  }

  @Override
  public void start() {
    repath = 0;
  }

  @Override
  public void stop() {
    owner = null;
    thrall.getNavigation().stop();
  }

  @Override
  public void tick() {
    if (owner == null) {
      return;
    }
    thrall.getLookControl().lookAt(owner, 10.0F, thrall.getMaxLookPitchChange());
    if (--repath > 0) {
      return;
    }
    repath = getTickCount(REPATH_TICKS);
    if (FollowRules.teleports(
            thrall.distanceTo(owner), owner.isOnGround(), owner.isInLava() || owner.isOnFire())
        && thrall
            .getWorld()
            .isSpaceEmpty(thrall, thrall.getType().getDimensions().getBoxAt(owner.getPos()))) {
      thrall.refreshPositionAndAngles(
          owner.getX(), owner.getY(), owner.getZ(), thrall.getYaw(), thrall.getPitch());
      thrall.getNavigation().stop();
      return;
    }
    thrall.getNavigation().startMovingTo(owner, SPEED);
  }
}
