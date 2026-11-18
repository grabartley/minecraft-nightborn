package com.grahambartley.nightborn.servant.goal;

import com.grahambartley.nightborn.servant.FollowRules;
import com.grahambartley.nightborn.servant.Stanced;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class StancePostGoal extends Goal {

  static final double SPEED = 0.9;

  private final PathAwareEntity keeper;
  private final Stanced stanced;

  public StancePostGoal(final PathAwareEntity keeper, final Stanced stanced) {
    this.keeper = keeper;
    this.stanced = stanced;
    setControls(EnumSet.of(Control.MOVE));
  }

  private boolean returns() {
    final double fromPost =
        Math.sqrt(keeper.squaredDistanceTo(Vec3d.ofBottomCenter(stanced.post())));
    return stanced
        .stance()
        .filter(stance -> FollowRules.returnsToPost(stance, fromPost))
        .isPresent();
  }

  @Override
  public boolean canStart() {
    return keeper.getTarget() == null && returns();
  }

  @Override
  public boolean shouldContinue() {
    return keeper.getTarget() == null && returns() && !keeper.getNavigation().isIdle();
  }

  @Override
  public void start() {
    final BlockPos post = stanced.post();
    keeper.getNavigation().startMovingTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, SPEED);
  }
}
