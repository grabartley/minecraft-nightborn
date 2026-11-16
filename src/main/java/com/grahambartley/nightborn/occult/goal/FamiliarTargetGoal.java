package com.grahambartley.nightborn.occult.goal;

import com.grahambartley.nightborn.occult.FamiliarRules;
import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import com.grahambartley.nightborn.servant.Servants;
import com.grahambartley.nightborn.thrall.TargetRules;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
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
    rethink = RETHINK_TICKS;
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
    final LivingEntity owner = familiar.ownerHere();
    if (owner != null) {
      if (allowed(owner.getAttacker())) {
        return owner.getAttacker();
      }
      if (owner instanceof MobEntity mob && allowed(mob.getTarget())) {
        return mob.getTarget();
      }
      if (TargetRules.ownerStillFighting(owner.age, owner.getLastAttackTime())
          && allowed(owner.getAttacking())) {
        return owner.getAttacking();
      }
    }
    return allowed(familiar.getAttacker()) ? familiar.getAttacker() : null;
  }

  private boolean allowed(@Nullable final LivingEntity candidate) {
    return candidate != null
        && TargetRules.mayAttack(
            candidate.isAlive(),
            familiar.isOwnedBy(candidate),
            Servants.sameMaster(familiar, candidate),
            candidate instanceof PlayerEntity player
                && (player.isCreative() || player.isSpectator()))
        && canTrack(candidate, TargetPredicate.DEFAULT);
  }
}
