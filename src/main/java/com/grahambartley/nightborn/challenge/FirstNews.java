package com.grahambartley.nightborn.challenge;

import net.minecraft.text.Text;

public final class FirstNews {

  public static final String UNNAMED = ".unnamed";

  private FirstNews() {}

  public static boolean named(final boolean vampire, final boolean factionsShown) {
    return !vampire || factionsShown;
  }

  public static Text line(
      final ServerFirst first,
      final Text name,
      final boolean vampire,
      final boolean factionsShown) {
    return named(vampire, factionsShown)
        ? Text.translatable(first.key(), name)
        : Text.translatable(first.key() + UNNAMED);
  }
}
