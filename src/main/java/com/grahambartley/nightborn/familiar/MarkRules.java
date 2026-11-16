package com.grahambartley.nightborn.familiar;

public final class MarkRules {

  private MarkRules() {}

  public static boolean mayMark(
      final boolean errandContainer, final boolean ownerMayUse, final boolean unlocked) {
    return errandContainer && ownerMayUse && unlocked;
  }
}
