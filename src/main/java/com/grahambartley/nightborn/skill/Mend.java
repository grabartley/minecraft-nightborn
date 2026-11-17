package com.grahambartley.nightborn.skill;

import org.jetbrains.annotations.Nullable;

public record Mend(float perTick, int ticksLeft) {

  @Nullable
  public Mend next(final boolean helpless) {
    return !helpless && ticksLeft > 1 ? new Mend(perTick, ticksLeft - 1) : null;
  }
}
