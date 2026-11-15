package com.grahambartley.nightborn.client.anim;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.math.Vec3d;

public record RigGeometry(Map<String, Bone> bones, Map<String, Locator> locators) {

  public static final RigGeometry NONE = new RigGeometry(Map.of(), Map.of());

  public record Bone(Vec3d pivot, Optional<String> parent) {}

  public record Locator(String bone, Vec3d position) {}

  public RigGeometry {
    bones = Map.copyOf(bones);
    locators = Map.copyOf(locators);
  }

  public Optional<Bone> bone(final String name) {
    return Optional.ofNullable(bones.get(name));
  }

  public Optional<Locator> locator(final String name) {
    return Optional.ofNullable(locators.get(name));
  }

  public static RigGeometry read(final JsonObject file) {
    final Map<String, Bone> bones = new HashMap<>();
    final Map<String, Locator> locators = new HashMap<>();
    for (final JsonElement element :
        file.getAsJsonArray("minecraft:geometry")
            .get(0)
            .getAsJsonObject()
            .getAsJsonArray("bones")) {
      final JsonObject bone = element.getAsJsonObject();
      final String name = bone.get("name").getAsString();
      bones.put(
          name,
          new Bone(
              ClipFile.vector(bone.get("pivot")),
              bone.has("parent")
                  ? Optional.of(bone.get("parent").getAsString())
                  : Optional.empty()));
      if (bone.has("locators")) {
        bone.getAsJsonObject("locators")
            .entrySet()
            .forEach(
                locator ->
                    locators.put(
                        locator.getKey(), new Locator(name, ClipFile.vector(locator.getValue()))));
      }
    }
    return new RigGeometry(bones, locators);
  }
}
