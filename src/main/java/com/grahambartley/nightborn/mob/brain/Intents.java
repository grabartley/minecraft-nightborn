package com.grahambartley.nightborn.mob.brain;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.MobEntity;

public final class Intents {

  private Intents() {}

  public static Intent read(final MobEntity mob) {
    final List<Intent> reported = new ArrayList<>(2);
    for (final PrioritizedGoal goal : mob.goalSelector.getGoals()) {
      if (goal.isRunning() && goal.getGoal() instanceof IntentGoal intentGoal) {
        reported.add(intentGoal.intent());
      }
    }
    return pick(reported, mob.getTarget() != null);
  }

  public static Intent pick(final List<Intent> reported, final boolean hasTarget) {
    if (reported.contains(Intent.RETREAT)) {
      return Intent.RETREAT;
    }
    if (hasTarget) {
      return reported.contains(Intent.CLOSE_IN) ? Intent.CLOSE_IN : Intent.FIGHT;
    }
    return reported.stream()
        .filter(intent -> intent != Intent.FIGHT && intent != Intent.CLOSE_IN)
        .findFirst()
        .orElse(Intent.REST);
  }
}
