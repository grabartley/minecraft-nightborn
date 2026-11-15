package com.grahambartley.nightborn.mob;

public final class LandingEdge {

  private boolean levitating;

  public boolean landed(final boolean nowLevitating) {
    final boolean landed = levitating && !nowLevitating;
    levitating = nowLevitating;
    return landed;
  }
}
