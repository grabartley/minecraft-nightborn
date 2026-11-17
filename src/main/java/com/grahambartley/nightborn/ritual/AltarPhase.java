package com.grahambartley.nightborn.ritual;

import net.minecraft.util.StringIdentifiable;

public enum AltarPhase implements StringIdentifiable {
  IDLE("idle"),
  READY("relics_placed"),
  RITUAL_ACTIVE("ritual_active");

  static final int ACTIVE_LIGHT = 10;

  private final String id;

  AltarPhase(final String id) {
    this.id = id;
  }

  public static AltarPhase of(final boolean active, final boolean ready) {
    if (active) {
      return RITUAL_ACTIVE;
    }
    return ready ? READY : IDLE;
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
