package com.grahambartley.nightborn.mob.combat;

import net.minecraft.util.Identifier;

public record Tell(Identifier power, long castAt) {

  public enum Step {
    WAIT,
    CAST,
    DROP
  }

  public Step step(final boolean hasTarget, final long now) {
    if (!hasTarget) {
      return Step.DROP;
    }
    return now >= castAt ? Step.CAST : Step.WAIT;
  }
}
