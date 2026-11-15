package com.grahambartley.nightborn.decor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.util.math.Vec3d;

final class GeoFlames {

  private static final double PIXEL = 16.0;

  private GeoFlames() {}

  static Set<Vec3d> basesIn(final String art) throws IOException {
    final Path geo =
        Path.of("src/client/resources/assets/nightborn/geo/block/" + art + ".geo.json");
    return StreamSupport.stream(
            JsonParser.parseString(Files.readString(geo))
                .getAsJsonObject()
                .getAsJsonArray("minecraft:geometry")
                .get(0)
                .getAsJsonObject()
                .getAsJsonArray("bones")
                .spliterator(),
            false)
        .map(JsonElement::getAsJsonObject)
        .filter(bone -> bone.get("name").getAsString().startsWith("flame_"))
        .map(bone -> bone.getAsJsonArray("pivot"))
        .map(
            pivot ->
                new Vec3d(
                    (8 - pivot.get(0).getAsDouble()) / PIXEL,
                    pivot.get(1).getAsDouble() / PIXEL,
                    (8 + pivot.get(2).getAsDouble()) / PIXEL))
        .collect(Collectors.toSet());
  }
}
