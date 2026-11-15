package com.grahambartley.nightborn.client;

import java.util.Locale;
import net.minecraft.SharedConstants;

public final class PowerSlotView {

  public static final int ICON = 16;
  static final int MAX_KEY_LABEL = 2;

  private PowerSlotView() {}

  public static long seconds(final long ticks) {
    final int perSecond = SharedConstants.TICKS_PER_SECOND;
    return Math.max(0L, (ticks + perSecond - 1) / perSecond);
  }

  public static int shadedRows(final float fraction) {
    return Math.round(ICON * Math.max(0.0F, Math.min(1.0F, fraction)));
  }

  public static String keyLabel(final String bound, final boolean unbound) {
    if (unbound) {
      return "";
    }
    final String trimmed = bound.trim();
    if (trimmed.length() <= MAX_KEY_LABEL) {
      return trimmed.toUpperCase(Locale.ROOT);
    }
    final String[] words = trimmed.split("\\s+");
    if (words.length == 1) {
      return trimmed.substring(0, MAX_KEY_LABEL).toUpperCase(Locale.ROOT);
    }
    final StringBuilder label = new StringBuilder();
    for (final String word : words) {
      if (!word.isEmpty() && label.length() < MAX_KEY_LABEL) {
        label.append(Character.isDigit(word.charAt(0)) ? word : word.substring(0, 1));
      }
    }
    final String shortened = label.toString().toUpperCase(Locale.ROOT);
    return shortened.length() > MAX_KEY_LABEL ? shortened.substring(0, MAX_KEY_LABEL) : shortened;
  }
}
