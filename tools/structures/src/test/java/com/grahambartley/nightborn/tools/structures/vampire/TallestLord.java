package com.grahambartley.nightborn.tools.structures.vampire;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TallestLord {

  static final Path LOOK =
      Path.of("../../src/main/resources/data/nightborn/nightborn/vampire_look/lord.json");
  private static final Pattern MAX_HEIGHT = Pattern.compile("\"max_height\"\\s*:\\s*([0-9.]+)");

  private TallestLord() {}

  public static double height() {
    try {
      final Matcher matcher = MAX_HEIGHT.matcher(Files.readString(LOOK));
      if (!matcher.find()) {
        throw new IllegalStateException(LOOK + " names no height of its own");
      }
      return Double.parseDouble(matcher.group(1));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static int headroom() {
    return (int) Math.ceil(height());
  }
}
