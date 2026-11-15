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

public final class ConfigFile {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFile.class);
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final Path path;
  private final Clock clock;

  public ConfigFile(final Path path, final Clock clock) {
    this.path = path;
    this.clock = clock;
  }

  public NightbornConfig loadOrCreate() {
    if (!Files.exists(path)) {
      save(NightbornConfig.DEFAULTS);
      return NightbornConfig.DEFAULTS;
    }
    return load();
  }

  public NightbornConfig load() {
    final String text;
    try {
      text = Files.readString(path, StandardCharsets.UTF_8);
    } catch (final IOException ex) {
      LOGGER.warn(
          "Could not read Nightborn settings at {}, using defaults: {}", path, ex.getMessage());
      return NightbornConfig.DEFAULTS;
    }
    try {
      final JsonElement root = JsonParser.parseString(text);
      if (!root.isJsonObject()) {
        throw new JsonParseException("the file is not a JSON object");
      }
      final ConfigJson.Read read = ConfigJson.read(root.getAsJsonObject());
      read.problems().forEach(problem -> LOGGER.warn("Nightborn settings: {}", problem));
      return read.config();
    } catch (final JsonParseException ex) {
      final Path kept = keepBrokenFile();
      LOGGER.error(
          "Nightborn settings at {} are broken, kept as {} and using defaults: {}",
          path,
          kept,
          ex.getMessage());
      return NightbornConfig.DEFAULTS;
    }
  }

  public boolean save(final NightbornConfig config) {
    try {
      Files.createDirectories(path.getParent());
      final Path temp = path.resolveSibling(path.getFileName() + ".tmp");
      Files.writeString(temp, GSON.toJson(ConfigJson.write(config)), StandardCharsets.UTF_8);
      Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (final IOException ex) {
      LOGGER.error("Could not save Nightborn settings at {}: {}", path, ex.getMessage());
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
      LOGGER.error("Could not keep the broken Nightborn settings file: {}", ex.getMessage());
      return path;
    }
  }
}
