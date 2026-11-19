package com.grahambartley.nightborn.accessory;

public final class FaceLearning {

  private FaceLearning() {}

  public static boolean learns(final double roll, final double share) {
    return roll < Math.max(0.0, share);
  }
}
