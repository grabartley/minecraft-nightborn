package com.grahambartley.nightborn.mind;

public final class MindGuard {

  private MindGuard() {}

  public static boolean blocks(
      final boolean attackerMesmerised,
      final boolean attackerIsPlayer,
      final boolean mesmerisedByVictim) {
    return attackerMesmerised && (!attackerIsPlayer || mesmerisedByVictim);
  }
}
