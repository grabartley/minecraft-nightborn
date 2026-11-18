package com.grahambartley.nightborn.tools.structures.vampire;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class TallestVampire {

  static final Path BUILDS = Path.of("../../src/main/resources/data/nightborn/nightborn/vampire_build");
  private static final Pattern MAX_HEIGHT = Pattern.compile("\"max_height\"\\s*:\\s*([0-9.]+)");

  private TallestVampire() {}

  public static double height() {
    try (Stream<Path> builds = Files.list(BUILDS)) {
      return builds
          .filter(path -> path.toString().endsWith(".json"))
          .mapToDouble(TallestVampire::maxHeight)
          .max()
          .orElseThrow();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static int headroom() {
    return (int) Math.ceil(height());
  }

  private static double maxHeight(final Path build) {
    try {
      final Matcher matcher = MAX_HEIGHT.matcher(Files.readString(build));
      return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
