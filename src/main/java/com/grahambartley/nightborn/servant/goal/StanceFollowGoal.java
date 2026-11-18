package com.grahambartley.nightborn.servant.goal;

import com.grahambartley.nightborn.servant.FollowRules;
import com.grahambartley.nightborn.servant.Stance;
import com.grahambartley.nightborn.servant.Stanced;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.jetbrains.annotations.Nullable;

public class StanceFollowGoal extends Goal {

  static final int REPATH_TICKS = 10;
  static final double SPEED = 1.0;

  private final PathAwareEntity follower;
  private final Stanced stanced;
  @Nullable private LivingEntity leader;
  private int repath;

  public StanceFollowGoal(final PathAwareEntity follower, final Stanced stanced) {
    this.follower = follower;
    this.stanced = stanced;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    final LivingEntity here = stanced.leaderHere();
    if (follower.getTarget() != null
        || !FollowRules.follows(
            stanced.stance().orElse(Stance.STAY),
            here != null,
            here == null ? 0.0 : follower.distanceTo(here))) {
      return false;
    }
    leader = here;
    return true;
  }

  @Override
  public boolean shouldContinue() {
    return leader != null
        && leader.isAlive()
        && follower.getTarget() == null
        && FollowRules.keepsFollowing(
            stanced.stance().orElse(Stance.STAY), true, follower.distanceTo(leader));
  }

  @Override
  public void start() {
    repath = 0;
  }

  @Override
  public void stop() {
    leader = null;
    follower.getNavigation().stop();
  }

  @Override
  public void tick() {
    if (leader == null) {
      return;
    }
    follower.getLookControl().lookAt(leader, 10.0F, follower.getMaxLookPitchChange());
    if (--repath > 0) {
      return;
    }
    repath = getTickCount(REPATH_TICKS);
    if (FollowRules.teleports(
            follower.distanceTo(leader),
            leader.isOnGround(),
            leader.isInLava() || leader.isOnFire())
        && follower
            .getWorld()
            .isSpaceEmpty(follower, follower.getType().getDimensions().getBoxAt(leader.getPos()))) {
      follower.refreshPositionAndAngles(
          leader.getX(), leader.getY(), leader.getZ(), follower.getYaw(), follower.getPitch());
      follower.getNavigation().stop();
      return;
    }
    follower.getNavigation().startMovingTo(leader, SPEED);
  }
}
