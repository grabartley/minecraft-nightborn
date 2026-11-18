package com.grahambartley.nightborn.raid;

import net.minecraft.util.StringIdentifiable;

public enum NoiseAct implements StringIdentifiable {
  WALK("walk"),
  RUN("run"),
  JUMP("jump"),
  BREAK("break"),
  PLACE("place"),
  BLOW("blow"),
  LID("lid");

  private final String id;

  NoiseAct(final String id) {
    this.id = id;
  }

  @Override
  public String asString() {
    return id;
  }
}
