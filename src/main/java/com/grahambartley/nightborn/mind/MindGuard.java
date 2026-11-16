package com.grahambartley.nightborn.mind;

public final class MindGuard {

  private MindGuard() {}

  public static boolean blocks(
      final boolean attackerMesmerised,
      final boolean attackerIsPlayer,
      final boolean mesmerisedByVictim,
      final boolean explosion) {
    return attackerMesmerised && !explosion && (!attackerIsPlayer || mesmerisedByVictim);
  }
}
