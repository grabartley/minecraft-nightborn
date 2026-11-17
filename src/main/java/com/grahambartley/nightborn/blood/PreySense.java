package com.grahambartley.nightborn.blood;

public final class PreySense {

  private PreySense() {}

  public static boolean reveals(
      final double sense, final double distanceSquared, final boolean living, final boolean self) {
    return sense > 0.0 && living && !self && distanceSquared <= sense * sense;
  }
}
