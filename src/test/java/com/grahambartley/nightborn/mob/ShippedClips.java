package com.grahambartley.nightborn.mob;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class ShippedClips {

  private static final Path ANIMATIONS =
      Path.of("src/client/resources/assets/nightborn/animations/entity");

  private ShippedClips() {}

  public static Set<String> vampire() {
    return of("vampire");
  }

  public static Set<String> of(final String rig) {
    try {
      return JsonParser.parseString(Files.readString(ANIMATIONS.resolve(rig + ".animation.json")))
          .getAsJsonObject()
          .getAsJsonObject("animations")
          .keySet();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
