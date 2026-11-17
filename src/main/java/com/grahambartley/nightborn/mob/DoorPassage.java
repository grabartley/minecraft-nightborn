package com.grahambartley.nightborn.mob;

import net.minecraft.entity.ai.goal.LongDoorInteractGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.MobEntity;

public final class DoorPassage {

  static final int PRIORITY = 1;

  private DoorPassage() {}

  public static void allow(final MobEntity mob) {
    ((MobNavigation) mob.getNavigation()).setCanPathThroughDoors(true);
    mob.goalSelector.add(PRIORITY, new LongDoorInteractGoal(mob, true));
  }
}
