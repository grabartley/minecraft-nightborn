package com.grahambartley.nightborn.feeding;

public final class Bottling {

  private Bottling() {}

  public record Fill(int bottles, float leftover) {}

  public static Fill fill(final float held, final int bottleSize, final int emptyBottles) {
    if (held <= 0.0F || bottleSize <= 0 || emptyBottles <= 0) {
      return new Fill(0, Math.max(0.0F, held));
    }
    final int bottles = Math.min(emptyBottles, (int) Math.floor(held / bottleSize));
    return new Fill(bottles, held - bottles * (float) bottleSize);
  }
}
