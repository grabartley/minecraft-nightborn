package com.grahambartley.nightborn.challenge;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

final class ChallengeDataFiles {

  static final Path DATA = Path.of("src/main/resources/data/nightborn");

  private ChallengeDataFiles() {}

  static <T> Map<String, T> load(final String folder, final Codec<T> codec) throws IOException {
    final Map<String, T> loaded = new TreeMap<>();
    final Path root = DATA.resolve("nightborn").resolve(folder);
    try (Stream<Path> files = Files.list(root)) {
      for (final Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
        final String name = file.getFileName().toString().replace(".json", "");
        loaded.put(
            name,
            codec
                .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
                .getOrThrow());
      }
    }
    return loaded;
  }
}
