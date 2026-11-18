package com.grahambartley.nightborn.tools.structures.vampire;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;

public record BloodStainRules(
    int clusters, int radius, int floorSplatters, int pools, int wallSplatters, int wallMaxHeight) {

  public static final String RESOURCE = "/blood_stains.json";

  public static BloodStainRules forPath(final String path) {
    return forPath(load(), path);
  }

  static BloodStainRules forPath(final JsonObject data, final String path) {
    final JsonObject rules = data.getAsJsonObject("default").deepCopy();
    data.getAsJsonObject("structures").entrySet().stream()
        .filter(entry -> path.startsWith(entry.getKey()))
        .max(Comparator.comparingInt(entry -> entry.getKey().length()))
        .map(Map.Entry::getValue)
        .map(JsonElement::getAsJsonObject)
        .ifPresent(
            override -> override.entrySet().forEach(e -> rules.add(e.getKey(), e.getValue())));
    return new BloodStainRules(
        rules.get("clusters").getAsInt(),
        rules.get("radius").getAsInt(),
        rules.get("floor_splatters").getAsInt(),
        rules.get("pools").getAsInt(),
        rules.get("wall_splatters").getAsInt(),
        rules.get("wall_max_height").getAsInt());
  }

  static JsonObject load() {
    try (InputStream in = BloodStainRules.class.getResourceAsStream(RESOURCE);
        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      return JsonParser.parseReader(reader).getAsJsonObject();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
