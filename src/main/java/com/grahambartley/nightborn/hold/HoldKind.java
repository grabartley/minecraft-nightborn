package com.grahambartley.nightborn.hold;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum HoldKind implements StringIdentifiable {
  PIN("pin"),
  STAGGER("stagger");

  public static final Codec<HoldKind> CODEC = StringIdentifiable.createCodec(HoldKind::values);

  private final String name;

  HoldKind(final String name) {
    this.name = name;
  }

  @Override
  public String asString() {
    return name;
  }

  public boolean stopsAttacks() {
    return this == STAGGER;
  }

  public boolean stopsPower(final boolean moves) {
    return this == STAGGER || moves;
  }
}
