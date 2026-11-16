package com.grahambartley.nightborn.thrall.goal;

import com.grahambartley.nightborn.thrall.FollowRules;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ThrallPostGoal extends Goal {

  static final double SPEED = 0.9;

  private final ThrallEntity thrall;

  public ThrallPostGoal(final ThrallEntity thrall) {
    this.thrall = thrall;
    setControls(EnumSet.of(Control.MOVE));
  }

  private double fromPost() {
    return Math.sqrt(thrall.squaredDistanceTo(Vec3d.ofBottomCenter(thrall.post())));
  }

  @Override
  public boolean canStart() {
    return thrall.getTarget() == null && FollowRules.returnsToPost(thrall.mode(), fromPost());
  }

  @Override
  public boolean shouldContinue() {
    return thrall.getTarget() == null
        && FollowRules.returnsToPost(thrall.mode(), fromPost())
        && !thrall.getNavigation().isIdle();
  }

  @Override
  public void start() {
    final BlockPos post = thrall.post();
    thrall.getNavigation().startMovingTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, SPEED);
  }
}
