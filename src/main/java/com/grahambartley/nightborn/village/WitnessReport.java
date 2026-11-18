package com.grahambartley.nightborn.village;

import java.util.UUID;
import net.minecraft.util.math.BlockPos;

public record WitnessReport(
    UUID witness, UUID vampire, SuspicionCause cause, BlockPos bell, long seenAt, long night) {

  public static final double AT_BELL = 3.0;

  public enum Outcome {
    RUNNING,
    TOLD,
    SILENCED
  }

  public Outcome outcome(
      final boolean witnessAlive,
      final double squaredDistanceToBell,
      final long now,
      final int reportTicks) {
    if (!witnessAlive) {
      return Outcome.SILENCED;
    }
    return squaredDistanceToBell <= AT_BELL * AT_BELL || now - seenAt >= reportTicks
        ? Outcome.TOLD
        : Outcome.RUNNING;
  }

  public boolean sameSighting(final WitnessReport other) {
    return witness.equals(other.witness)
        && vampire.equals(other.vampire)
        && cause == other.cause
        && night == other.night;
  }
}
