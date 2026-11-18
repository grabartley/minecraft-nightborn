package com.grahambartley.nightborn.servant;

public final class MarkRules {

  private MarkRules() {}

  public static boolean mayMark(
      final boolean errandContainer, final boolean ownerMayUse, final boolean unlocked) {
    return errandContainer && ownerMayUse && unlocked;
  }
}
