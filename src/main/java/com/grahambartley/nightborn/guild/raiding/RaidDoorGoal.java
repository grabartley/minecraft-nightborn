package com.grahambartley.nightborn.guild.raiding;

import java.util.function.BooleanSupplier;
import net.minecraft.entity.ai.goal.LongDoorInteractGoal;
import net.minecraft.entity.mob.MobEntity;

public class RaidDoorGoal extends LongDoorInteractGoal {

  private final BooleanSupplier raiding;

  public RaidDoorGoal(final MobEntity raider, final BooleanSupplier raiding) {
    super(raider, true);
    this.raiding = raiding;
  }

  @Override
  public boolean canStart() {
    return raiding.getAsBoolean() && super.canStart();
  }
}
