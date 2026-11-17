package com.grahambartley.nightborn.client;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class BloodBarIcons {

  private BloodBarIcons() {}

  @Nullable
  public static Identifier fill(final int icon, final int blood) {
    final int full = icon * 2 + 2;
    if (blood >= full) {
      return BloodBar.FULL;
    }
    return blood == full - 1 ? BloodBar.HALF : null;
  }
}
