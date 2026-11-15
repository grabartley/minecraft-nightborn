package com.grahambartley.nightborn.structure;

public final class StructureSpread {

  private static final int PERCENT = 100;

  private StructureSpread() {}

  public static int spacing(final int baseSpacing, final int rarityPercent) {
    return Math.max(1, (int) ((long) baseSpacing * rarityPercent / PERCENT));
  }

  public static int separation(
      final int spacing, final int baseSeparation, final int rarityPercent) {
    return Math.clamp((long) baseSeparation * rarityPercent / PERCENT, 0, spacing - 1);
  }
}
