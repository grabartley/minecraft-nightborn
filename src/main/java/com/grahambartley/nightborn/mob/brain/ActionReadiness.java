package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.PowerUse;
import net.minecraft.util.Identifier;

public record ActionReadiness(
    Identifier id, PowerUse.Verdict verdict, int cooldownTicks, boolean running) {

  public static ActionReadiness of(
      final Identifier id, final boolean ready, final int cooldownTicks) {
    return new ActionReadiness(
        id, ready ? PowerUse.Verdict.READY : PowerUse.Verdict.COOLING, cooldownTicks, false);
  }
}
