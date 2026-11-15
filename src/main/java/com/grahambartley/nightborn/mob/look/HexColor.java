package com.grahambartley.nightborn.mob.look;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public final class HexColor {

  public static final Codec<Integer> CODEC =
      Codec.STRING.comapFlatMap(HexColor::parse, HexColor::format);

  private static final int DIGITS = 6;
  private static final int HEX = 16;

  private HexColor() {}

  public static DataResult<Integer> parse(final String text) {
    if (text.length() != DIGITS + 1 || text.charAt(0) != '#') {
      return DataResult.error(() -> "Not a #RRGGBB colour: " + text);
    }
    try {
      return DataResult.success(Integer.parseInt(text.substring(1), HEX));
    } catch (final NumberFormatException e) {
      return DataResult.error(() -> "Not a #RRGGBB colour: " + text);
    }
  }

  public static String format(final int rgb) {
    return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
  }
}
