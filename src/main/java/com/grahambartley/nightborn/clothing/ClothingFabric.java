package com.grahambartley.nightborn.clothing;

public enum ClothingFabric {
  CLOTH(true),
  LEATHER(false);

  private final boolean dyeable;

  ClothingFabric(final boolean dyeable) {
    this.dyeable = dyeable;
  }

  public boolean dyeable() {
    return this.dyeable;
  }
}
