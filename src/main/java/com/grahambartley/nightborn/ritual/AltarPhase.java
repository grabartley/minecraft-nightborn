package com.grahambartley.nightborn.ritual;

import net.minecraft.util.StringIdentifiable;

public enum AltarPhase implements StringIdentifiable {
  IDLE("idle"),
  RELICS_PLACED("relics_placed"),
  RITUAL_ACTIVE("ritual_active");

  static final int ACTIVE_LIGHT = 10;

  private final String id;

  AltarPhase(final String id) {
    this.id = id;
  }

  public static AltarPhase of(final boolean active, final int placed) {
    if (active) {
      return RITUAL_ACTIVE;
    }
    return placed > 0 ? RELICS_PLACED : IDLE;
  }

  public String clip() {
    return id;
  }

  public int light() {
    return this == RITUAL_ACTIVE ? ACTIVE_LIGHT : 0;
  }

  @Override
  public String asString() {
    return id;
  }
}
