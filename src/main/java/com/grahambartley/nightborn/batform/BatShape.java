package com.grahambartley.nightborn.batform;

public final class BatShape {

  private boolean bat;

  public boolean changed(final boolean nowBat) {
    final boolean changed = nowBat != bat;
    bat = nowBat;
    return changed;
  }
}
