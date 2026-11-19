package com.grahambartley.nightborn.tools.structures.vampire;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class TallestVampire {

  static final Path DATA = Path.of("../../src/main/resources/data/nightborn/nightborn");
  static final Path BUILDS = DATA.resolve("vampire_build");
  static final Path LOOKS = DATA.resolve("vampire_look");
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
    return headroom(height());
  }

  public static double height(final String look) {
    final double height = maxHeight(LOOKS.resolve(look + ".json"));
    if (height == 0.0) {
      throw new IllegalStateException(look + " names no build of its own");
    }
    return height;
  }

  public static int headroom(final double height) {
    return (int) Math.ceil(height);
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
