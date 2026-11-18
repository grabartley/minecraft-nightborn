package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.clothing.Garment;
import java.util.Locale;

public enum AccessoryPiece implements Garment {
  MOURNING_VEIL(true, 0x1A1719, true),
  HIGH_COLLARED_CAPE(true, 0x5A1A24, true),
  ONYX_RING(false, 0xFFFFFF, false),
  VELVET_CHOKER(false, 0xFFFFFF, true),
  CAMEO_BROOCH(false, 0xFFFFFF, true),
  LACE_GLOVES(false, 0xFFFFFF, true),
  BRASS_BUCKLED_BELT(false, 0xFFFFFF, true);

  private final boolean dyeable;
  private final int undyedColor;
  private final boolean drawn;

  AccessoryPiece(final boolean dyeable, final int undyedColor, final boolean drawn) {
    this.dyeable = dyeable;
    this.undyedColor = undyedColor;
    this.drawn = drawn;
  }

  @Override
  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  @Override
  public boolean dyeable() {
    return this.dyeable;
  }

  @Override
  public int undyedColor() {
    return this.undyedColor;
  }

  public boolean drawn() {
    return this.drawn;
  }
}
