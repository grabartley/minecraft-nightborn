package com.grahambartley.nightborn.client.moment;

public final class CeremonyTitle {

  static final float LARGEST = 2.0F;
  static final int MARGIN = 24;

  private CeremonyTitle() {}

  public static float scale(final int textWidth, final int cardWidth) {
    final int room = cardWidth - 2 * MARGIN;
    if (textWidth <= 0 || room <= 0) {
      return LARGEST;
    }
    return Math.min(LARGEST, room / (float) textWidth);
  }
}
