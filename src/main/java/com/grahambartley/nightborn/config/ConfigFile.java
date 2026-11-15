package com.grahambartley.nightborn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigFile<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFile.class);
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final Path path;
  private final ConfigFormat<T> format;
  private final Clock clock;

  public ConfigFile(final Path path, final ConfigFormat<T> format, final Clock clock) {
    this.path = path;
    this.format = format;
    this.clock = clock;
  }

  public T loadOrCreate() {
    if (!Files.exists(path)) {
      save(format.defaults());
      return format.defaults();
    }
    return load();
  }

  public T load() {
    final String text;
    try {
      text = Files.readString(path, StandardCharsets.UTF_8);
    } catch (final IOException ex) {
      LOGGER.warn("Could not read Nightborn file at {}, using defaults: {}", path, ex.getMessage());
      return format.defaults();
    }
    try {
      final JsonElement root = JsonParser.parseString(text);
      if (!root.isJsonObject()) {
        throw new JsonParseException("the file is not a JSON object");
      }
      final ConfigFormat.Read<T> read = format.read(root.getAsJsonObject());
      read.problems().forEach(problem -> LOGGER.warn("Nightborn file at {}: {}", path, problem));
      return read.value();
    } catch (final JsonParseException ex) {
      final Path kept = keepBrokenFile();
      LOGGER.error(
          "Nightborn file at {} is broken, kept as {} and using defaults: {}",
          path,
          kept,
          ex.getMessage());
      return format.defaults();
    }
  }

  public boolean save(final T value) {
    try {
      Files.createDirectories(path.getParent());
      final Path temp = path.resolveSibling(path.getFileName() + ".tmp");
      Files.writeString(temp, GSON.toJson(format.write(value)), StandardCharsets.UTF_8);
      Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (final IOException ex) {
      LOGGER.error("Could not save Nightborn file at {}: {}", path, ex.getMessage());
      return false;
    }
  }

  Path brokenCopyPath() {
    final String name = path.getFileName().toString();
    final String base = name.endsWith(".json") ? name.substring(0, name.length() - 5) : name;
    final String stamp = DateTimeFormatter.ISO_INSTANT.format(clock.instant()).replace(":", "-");
    return path.resolveSibling(base + ".broken." + stamp + ".json");
  }

  private Path keepBrokenFile() {
    final Path broken = brokenCopyPath();
    try {
      Files.move(path, broken, StandardCopyOption.REPLACE_EXISTING);
      return broken;
    } catch (final IOException ex) {
      LOGGER.error("Could not keep the broken Nightborn file: {}", ex.getMessage());
      return path;
    }
  }
}
