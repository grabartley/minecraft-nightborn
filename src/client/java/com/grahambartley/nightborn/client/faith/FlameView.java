package com.grahambartley.nightborn.client.faith;

public final class FlameView {

  static final double NEAR = 1.6;

  private FlameView() {}

  public static boolean hides(
      final boolean firstPerson, final boolean holdsFlame, final double distanceSquared) {
    return firstPerson && holdsFlame && distanceSquared < NEAR * NEAR;
  }
}
