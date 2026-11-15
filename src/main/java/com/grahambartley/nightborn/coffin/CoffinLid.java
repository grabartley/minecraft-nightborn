package com.grahambartley.nightborn.coffin;

public final class CoffinLid {

  private CoffinLid() {}

  public static boolean openAfter(
      final boolean open, final boolean wasOccupied, final boolean occupied) {
    if (occupied == wasOccupied) {
      return open;
    }
    return !occupied;
  }
}
