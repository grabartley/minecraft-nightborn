package com.grahambartley.nightborn.client;

public record BloodBarLayout(int icons, int step) {

  static final int POINTS_PER_ICON = 2;

  public static BloodBarLayout of(final int capacity) {
    final int icons = Math.max(BloodBar.ICONS, (capacity + POINTS_PER_ICON - 1) / POINTS_PER_ICON);
    final int span = (BloodBar.ICONS - 1) * BloodBar.STEP;
    return new BloodBarLayout(icons, icons <= BloodBar.ICONS ? BloodBar.STEP : span / (icons - 1));
  }

  public int left(final int right, final int icon) {
    return right - icon * step - BloodBar.ICON_SIZE;
  }
}
