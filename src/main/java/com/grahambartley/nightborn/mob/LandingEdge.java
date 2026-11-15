package com.grahambartley.nightborn.mob;

public final class LandingEdge {

  private boolean levitating;
  private boolean landing;

  public boolean landed(final boolean nowLevitating) {
    final boolean landed = levitating && !nowLevitating;
    levitating = nowLevitating;
    if (landed) {
      landing = true;
    }
    return landed;
  }

  public boolean landing() {
    return landing;
  }

  public void shown(final boolean landPose, final boolean finished) {
    if (!landPose || finished) {
      landing = false;
    }
  }
}
