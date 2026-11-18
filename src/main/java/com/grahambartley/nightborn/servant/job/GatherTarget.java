package com.grahambartley.nightborn.servant.job;

public enum GatherTarget {
  CROP,
  WOOD,
  ORE,
  NONE;

  public static GatherTarget of(
      final boolean crop,
      final boolean ripe,
      final boolean wood,
      final boolean inATree,
      final boolean ore,
      final boolean pickaxeFits) {
    if (crop) {
      return ripe ? CROP : NONE;
    }
    if (wood) {
      return inATree ? WOOD : NONE;
    }
    return ore && pickaxeFits ? ORE : NONE;
  }

  public boolean replants() {
    return this == CROP;
  }

  public boolean wearsThePickaxe() {
    return this == ORE;
  }
}
