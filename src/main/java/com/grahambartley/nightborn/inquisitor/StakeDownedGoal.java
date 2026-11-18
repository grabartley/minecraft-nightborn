package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

public class StakeDownedGoal extends Goal {

  static final double REACH = 2.5;
  static final double SPEED = 1.2;
  static final int REPATH_TICKS = 10;

  private final PathAwareEntity mob;
  private int repath;

  public StakeDownedGoal(final PathAwareEntity mob) {
    this.mob = mob;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  public static boolean stakes(
      final boolean vampire, final boolean helpless, final double distance) {
    return vampire && helpless && distance <= REACH;
  }

  @Override
  public boolean canStart() {
    final LivingEntity target = mob.getTarget();
    return target != null && Vampires.isVampire(target) && Downed.helpless(target);
  }

  @Override
  public boolean shouldContinue() {
    return canStart();
  }

  @Override
  public void start() {
    repath = 0;
  }

  @Override
  public void stop() {
    mob.getNavigation().stop();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    final LivingEntity target = mob.getTarget();
    if (target == null) {
      return;
    }
    mob.getLookControl().lookAt(target, 30.0F, 30.0F);
    if (stakes(Vampires.isVampire(target), Downed.helpless(target), mob.distanceTo(target))) {
      mob.swingHand(Hand.MAIN_HAND);
      mob.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1.0F, 0.7F);
      target.damage(NightbornDamage.stake(mob.getWorld(), mob), Float.MAX_VALUE);
      return;
    }
    if (--repath <= 0) {
      repath = REPATH_TICKS;
      mob.getNavigation().startMovingTo(target, SPEED);
    }
  }
}
