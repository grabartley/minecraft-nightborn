package com.grahambartley.nightborn.occult.goal;

import com.grahambartley.nightborn.occult.FamiliarRules;
import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import com.grahambartley.nightborn.servant.ServantTargets;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import org.jetbrains.annotations.Nullable;

public class FamiliarTargetGoal extends TrackTargetGoal {

  static final int RETHINK_TICKS = 10;

  private final GhostlyFamiliarEntity familiar;
  @Nullable private LivingEntity chosen;
  private int rethink;

  public FamiliarTargetGoal(final GhostlyFamiliarEntity familiar) {
    super(familiar, false);
    this.familiar = familiar;
    setControls(EnumSet.of(Control.TARGET));
  }

  @Override
  public boolean canStart() {
    if (--rethink > 0 || !FamiliarRules.fights(familiar.mode())) {
      return false;
    }
    rethink = getTickCount(RETHINK_TICKS);
    chosen = pick();
    return chosen != null;
  }

  @Override
  public boolean shouldContinue() {
    return FamiliarRules.fights(familiar.mode())
        && allowed(familiar.getTarget())
        && super.shouldContinue();
  }

  @Override
  public void start() {
    familiar.setTarget(chosen);
    super.start();
  }

  @Nullable
  private LivingEntity pick() {
    return ServantTargets.mastersFoe(familiar, this::allowed);
  }

  private boolean allowed(@Nullable final LivingEntity candidate) {
    return candidate != null
        && ServantTargets.mayAttack(familiar, candidate)
        && canTrack(candidate, TargetPredicate.DEFAULT);
  }
}
