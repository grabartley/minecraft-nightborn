package com.grahambartley.nightborn.render;

public final class BatFormPose {

  static final float SWOOP_SWING = 0.6F;

  private BatFormPose() {}

  public static boolean swoops(final float limbSwingAmount) {
    return limbSwingAmount >= SWOOP_SWING;
  }
}
