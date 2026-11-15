package com.grahambartley.nightborn.mob;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class ShippedClips {

  private static final Path VAMPIRE =
      Path.of("src/client/resources/assets/nightborn/animations/entity/vampire.animation.json");

  private ShippedClips() {}

  public static Set<String> vampire() {
    try {
      return JsonParser.parseString(Files.readString(VAMPIRE))
          .getAsJsonObject()
          .getAsJsonObject("animations")
          .keySet();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
