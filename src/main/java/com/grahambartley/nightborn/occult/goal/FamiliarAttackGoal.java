package com.grahambartley.nightborn.occult.goal;

import com.grahambartley.nightborn.occult.FamiliarRules;
import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Hand;

public class FamiliarAttackGoal extends Goal {

  static final int ATTACK_COOLDOWN_TICKS = 20;
  static final double REACH = 1.6;
  static final double SPEED = 1.2;
  static final int REPATH_TICKS = 10;

  private final GhostlyFamiliarEntity familiar;
  private int cooldown;
  private int repath;

  public FamiliarAttackGoal(final GhostlyFamiliarEntity familiar) {
    this.familiar = familiar;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    final LivingEntity target = familiar.getTarget();
    return target != null && target.isAlive() && FamiliarRules.fights(familiar.mode());
  }

  @Override
  public boolean shouldContinue() {
    return canStart();
  }

  @Override
  public void stop() {
    familiar.getNavigation().stop();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    final LivingEntity target = familiar.getTarget();
    if (target == null) {
      return;
    }
    if (cooldown > 0) {
      cooldown--;
    }
    familiar.getLookControl().lookAt(target, 30.0F, 30.0F);
    final boolean sees = familiar.canSee(target);
    if (sees) {
      familiar.getNavigation().stop();
      familiar.getMoveControl().moveTo(target.getX(), target.getBodyY(0.6), target.getZ(), SPEED);
    } else if (--repath <= 0) {
      repath = getTickCount(REPATH_TICKS);
      familiar.getNavigation().startMovingTo(target, SPEED);
    }
    final double reach = REACH + target.getWidth() / 2.0;
    if (cooldown == 0 && sees && familiar.squaredDistanceTo(target) <= reach * reach) {
      familiar.swingHand(Hand.MAIN_HAND);
      familiar.tryAttack(target);
      cooldown = getTickCount(ATTACK_COOLDOWN_TICKS);
    }
  }
}
