package com.grahambartley.nightborn.occult.goal;

import com.grahambartley.nightborn.occult.FamiliarRules;
import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

public class FamiliarFollowGoal extends Goal {

  static final int REPATH_TICKS = 10;
  static final double SPEED = 1.0;

  private final GhostlyFamiliarEntity familiar;
  @Nullable private LivingEntity owner;
  private int repath;

  public FamiliarFollowGoal(final GhostlyFamiliarEntity familiar) {
    this.familiar = familiar;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    final LivingEntity here = familiar.ownerHere();
    if (familiar.getTarget() != null
        || familiar.mode().passesThroughBlocks()
        || !FamiliarRules.follows(here != null, here == null ? 0.0 : familiar.distanceTo(here))) {
      return false;
    }
    owner = here;
    return true;
  }

  @Override
  public boolean shouldContinue() {
    return owner != null
        && owner.isAlive()
        && familiar.getTarget() == null
        && !familiar.mode().passesThroughBlocks()
        && FamiliarRules.keepsFollowing(true, familiar.distanceTo(owner));
  }

  @Override
  public void start() {
    repath = 0;
  }

  @Override
  public void stop() {
    owner = null;
    familiar.getNavigation().stop();
  }

  @Override
  public void tick() {
    if (owner == null) {
      return;
    }
    familiar.getLookControl().lookAt(owner, 10.0F, familiar.getMaxLookPitchChange());
    if (--repath > 0) {
      return;
    }
    repath = getTickCount(REPATH_TICKS);
    if (FamiliarRules.teleports(familiar.mode(), familiar.distanceTo(owner))) {
      familiar.refreshPositionAndAngles(
          owner.getX(), owner.getBodyY(0.6), owner.getZ(), familiar.getYaw(), familiar.getPitch());
      familiar.getNavigation().stop();
      return;
    }
    familiar.getNavigation().startMovingTo(owner.getX(), owner.getBodyY(0.6), owner.getZ(), SPEED);
  }
}
