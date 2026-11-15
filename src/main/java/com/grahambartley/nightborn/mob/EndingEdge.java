package com.grahambartley.nightborn.mob;

public final class EndingEdge {

  private boolean active;
  private boolean ending;

  public boolean ended(final boolean nowActive) {
    final boolean ended = active && !nowActive;
    active = nowActive;
    if (ended) {
      ending = true;
    }
    return ended;
  }

  public boolean ending() {
    return ending;
  }

  public void shown(final boolean endingPose, final boolean finished) {
    if (!endingPose || finished) {
      ending = false;
    }
  }
}
