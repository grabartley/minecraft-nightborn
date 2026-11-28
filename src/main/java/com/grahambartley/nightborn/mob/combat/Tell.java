package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.brain.Aim;
import java.util.UUID;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record Tell(Identifier power, long castAt, Aim aim, @Nullable UUID ally, boolean inFight) {

  public enum Step {
    WAIT,
    CAST,
    DROP
  }

  public Tell(final Identifier power, final long castAt, final Aim aim) {
    this(power, castAt, aim, null, true);
  }

  public Step step(final boolean engaged, final long now) {
    if (inFight && !engaged && ally == null) {
      return Step.DROP;
    }
    return now >= castAt ? Step.CAST : Step.WAIT;
  }
}
