package com.grahambartley.nightborn.client;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class BloodBarIcons {

  private BloodBarIcons() {}

  public static int count(final int maxBlood) {
    return (Math.max(0, maxBlood) + 1) / 2;
  }

  public static int column(final int icon) {
    return icon % BloodBar.ICONS;
  }

  public static int row(final int icon) {
    return icon / BloodBar.ICONS;
  }

  @Nullable
  public static Identifier fill(final int icon, final int blood) {
    final int full = icon * 2 + 2;
    if (blood >= full) {
      return BloodBar.FULL;
    }
    return blood == full - 1 ? BloodBar.HALF : null;
  }
}
