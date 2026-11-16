package com.grahambartley.nightborn.tools.structures.vampire;

public enum Axis {
  X("west", "east"),
  Z("north", "south");

  private final String low;
  private final String high;

  Axis(final String low, final String high) {
    this.low = low;
    this.high = high;
  }

  public String low() {
    return low;
  }

  public String high() {
    return high;
  }

  public Axis across() {
    return this == X ? Z : X;
  }

  public int x(final int along, final int across) {
    return this == X ? along : across;
  }

  public int z(final int along, final int across) {
    return this == X ? across : along;
  }
}
