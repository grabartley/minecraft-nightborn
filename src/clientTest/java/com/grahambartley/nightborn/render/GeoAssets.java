package com.grahambartley.nightborn.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;

final class GeoAssets {

  static final Path ASSETS = Path.of("src/client/resources/assets");
  static final Path GEO = ASSETS.resolve("nightborn/geo");
  static final Path ENTITY_GEO = GEO.resolve("entity");
  static final Path ARMOR_GEO = GEO.resolve("armor");
  static final Path ENTITY_CLIPS = ASSETS.resolve("nightborn/animations/entity");

  private GeoAssets() {}

  static Path file(final Identifier id) {
    return ASSETS.resolve(id.getNamespace()).resolve(id.getPath());
  }

  static JsonObject read(final Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
  }

  static JsonObject geometry(final Path file) throws IOException {
    return read(file).getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
  }

  static JsonArray bones(final Path file) throws IOException {
    return geometry(file).getAsJsonArray("bones");
  }

  static Set<String> boneNames(final Path file) throws IOException {
    final Set<String> names = new HashSet<>();
    for (final JsonElement bone : bones(file)) {
      names.add(bone.getAsJsonObject().get("name").getAsString());
    }
    return names;
  }

  static JsonObject animations(final Path file) throws IOException {
    return read(file).getAsJsonObject("animations");
  }

  static List<Path> geoFiles(final Path dir) throws IOException {
    try (Stream<Path> files = Files.list(dir)) {
      return files.filter(file -> file.toString().endsWith(".geo.json")).sorted().toList();
    }
  }
}
