package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.brain.Aim;
import net.minecraft.util.Identifier;

public record Tell(Identifier power, long castAt, Aim aim) {

  public enum Step {
    WAIT,
    CAST,
    DROP
  }

  public Step step(final boolean engaged, final long now) {
    if (!engaged) {
      return Step.DROP;
    }
    return now >= castAt ? Step.CAST : Step.WAIT;
  }
}
