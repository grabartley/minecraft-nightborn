package com.grahambartley.nightborn.decor;

public enum FamilyMember {
  BASE(""),
  STAIRS("_stairs"),
  SLAB("_slab"),
  WALL("_wall"),
  FENCE("_fence");

  private static final String BRICKS = "_bricks";
  private static final String PLANKS = "_planks";

  private final String suffix;

  FamilyMember(final String suffix) {
    this.suffix = suffix;
  }

  public String idFor(final String baseId) {
    if (this == BASE) {
      return baseId;
    }
    if (baseId.endsWith(BRICKS)) {
      return baseId.substring(0, baseId.length() - 1) + this.suffix;
    }
    if (baseId.endsWith(PLANKS)) {
      return baseId.substring(0, baseId.length() - PLANKS.length()) + this.suffix;
    }
    return baseId + this.suffix;
  }
}
