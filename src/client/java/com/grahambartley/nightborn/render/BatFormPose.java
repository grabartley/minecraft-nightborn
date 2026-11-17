package com.grahambartley.nightborn.render;

public enum BatFormPose {
  HANG,
  REST,
  CRAWL,
  FLY,
  SWOOP;

  static final float CRAWL_SWING = 0.05F;

  public static BatFormPose of(
      final boolean hanging,
      final boolean onGround,
      final float limbSwingAmount,
      final boolean sprinting) {
    if (hanging) {
      return HANG;
    }
    if (onGround) {
      return limbSwingAmount > CRAWL_SWING ? CRAWL : REST;
    }
    return sprinting ? SWOOP : FLY;
  }
}
