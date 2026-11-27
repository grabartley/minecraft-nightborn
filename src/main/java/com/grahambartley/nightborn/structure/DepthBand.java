package com.grahambartley.nightborn.structure;

public record DepthBand(int bottom, int top) {

  public DepthBand within(final int worldBottom, final int worldTop) {
    return new DepthBand(Math.max(bottom, worldBottom), Math.min(top, worldTop));
  }

  public int height() {
    return Math.max(0, top - bottom);
  }
}
