package com.grahambartley.nightborn.fight;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum Cutting implements StringIdentifiable {
  WEAPON("weapon"),
  ALL("all"),
  MARKED("marked");

  public static final Codec<Cutting> CODEC = StringIdentifiable.createCodec(Cutting::values);

  private final String id;

  Cutting(final String id) {
    this.id = id;
  }

  @Override
  public String asString() {
    return id;
  }

  public boolean severing(final boolean marked, final boolean edged, final boolean melee) {
    return marked
        || switch (this) {
          case WEAPON -> edged;
          case ALL -> melee;
          case MARKED -> false;
        };
  }

  public boolean bars(final boolean marked) {
    return this == MARKED && !marked;
  }
}
