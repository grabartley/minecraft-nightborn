package com.grahambartley.nightborn.servant.goal;

import com.grahambartley.nightborn.servant.ServantTargets;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

public class MastersFoeGoal extends TrackTargetGoal {

  static final int RETHINK_TICKS = 10;

  @Nullable private LivingEntity chosen;
  private int rethink;

  public MastersFoeGoal(final MobEntity servant) {
    super(servant, false);
    setControls(EnumSet.of(Control.TARGET));
  }

  @Override
  public boolean canStart() {
    if (--rethink > 0) {
      return false;
    }
    rethink = getTickCount(RETHINK_TICKS);
    chosen = ServantTargets.mastersFoe(mob, this::allowed);
    return chosen != null;
  }

  @Override
  public boolean shouldContinue() {
    final LivingEntity next = ServantTargets.mastersFoe(mob, this::allowed);
    if (next != null && next != mob.getTarget()) {
      mob.setTarget(next);
    }
    return allowed(mob.getTarget()) && super.shouldContinue();
  }

  @Override
  public void start() {
    mob.setTarget(chosen);
    super.start();
  }

  private boolean allowed(@Nullable final LivingEntity candidate) {
    return candidate != null
        && ServantTargets.mayAttack(mob, candidate)
        && canTrack(candidate, TargetPredicate.DEFAULT);
  }
}
