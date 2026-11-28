package com.grahambartley.nightborn.guild;

import java.util.function.BooleanSupplier;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;

public final class CrossbowGoal extends ProjectileAttackGoal {

  static final double SPEED = 1.0;
  static final int INTERVAL_TICKS = 30;
  static final float RANGE = 14.0F;

  private final BooleanSupplier armed;

  public CrossbowGoal(final RangedAttackMob shooter, final BooleanSupplier armed) {
    super(shooter, SPEED, INTERVAL_TICKS, RANGE);
    this.armed = armed;
  }

  @Override
  public boolean canStart() {
    return armed.getAsBoolean() && super.canStart();
  }

  @Override
  public boolean shouldContinue() {
    return armed.getAsBoolean() && super.shouldContinue();
  }
}
