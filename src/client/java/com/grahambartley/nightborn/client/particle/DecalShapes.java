package com.grahambartley.nightborn.client.particle;

public record DecalShapes(int count) {

  public DecalShapes {
    if (count < 1) {
      throw new IllegalArgumentException("a decal needs at least one shape");
    }
  }

  public int rim(final int roll) {
    return Math.floorMod(roll, count);
  }

  public int body(final int roll) {
    return rim(roll) + count;
  }

  public int lastSprite() {
    return count * 2 - 1;
  }
}
