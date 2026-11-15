package com.grahambartley.nightborn.familiar;

public final class Loyalty {

  public static final int MIN = 0;
  public static final int MAX = 100;

  private Loyalty() {}

  public static int clamp(final int loyalty) {
    return Math.max(MIN, Math.min(MAX, loyalty));
  }

  public static int change(final int loyalty, final int delta) {
    return clamp(loyalty + delta);
  }
}
