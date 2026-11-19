package com.grahambartley.nightborn.oldblood;

import net.minecraft.util.StringIdentifiable;

public enum FontLevel implements StringIdentifiable {
  EMPTY("empty"),
  LOW("low"),
  HALF("half"),
  HIGH("high"),
  FULL("full");

  private static final FontLevel[] RISING = {EMPTY, LOW, HALF, HIGH};

  private final String id;

  FontLevel(final String id) {
    this.id = id;
  }

  public static FontLevel of(final long progress, final int refillDays) {
    if (FontRefill.full(progress)) {
      return FULL;
    }
    final long share = Math.max(0L, progress) * RISING.length / FontRefill.needed(refillDays);
    return RISING[(int) Math.min(share, RISING.length - 1)];
  }

  public boolean full() {
    return this == FULL;
  }

  public String clip() {
    return id;
  }

  @Override
  public String asString() {
    return id;
  }
}
