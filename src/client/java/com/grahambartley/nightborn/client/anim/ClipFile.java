package com.grahambartley.nightborn.client.anim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.Vec3d;

public final class ClipFile {

  static final String CATMULL_ROM = "catmullrom";

  private ClipFile() {}

  public static Map<String, AnimationClip> read(final JsonObject file) {
    final Map<String, AnimationClip> clips = new HashMap<>();
    file.getAsJsonObject("animations")
        .entrySet()
        .forEach(entry -> clips.put(entry.getKey(), clip(entry.getKey(), entry.getValue())));
    return Map.copyOf(clips);
  }

  static AnimationClip clip(final String name, final JsonElement element) {
    final JsonObject json = element.getAsJsonObject();
    final boolean loops = json.has("loop") && isTrue(json.get("loop"));
    final double length =
        json.has("animation_length") ? json.get("animation_length").getAsDouble() : 0.0;
    final Map<String, AnimationClip.Track> tracks = new HashMap<>();
    if (json.has("bones")) {
      json.getAsJsonObject("bones")
          .entrySet()
          .forEach(
              bone -> {
                final JsonObject channels = bone.getValue().getAsJsonObject();
                tracks.put(
                    bone.getKey(),
                    new AnimationClip.Track(
                        channel(channels.get("rotation")), channel(channels.get("position"))));
              });
    }
    return new AnimationClip(name, length, loops, tracks);
  }

  private static boolean isTrue(final JsonElement loop) {
    return loop.isJsonPrimitive() && loop.getAsJsonPrimitive().isBoolean() && loop.getAsBoolean();
  }

  static ClipChannel channel(final JsonElement element) {
    if (element == null) {
      return ClipChannel.EMPTY;
    }
    if (element.isJsonArray()) {
      return new ClipChannel(List.of(new ClipChannel.Key(0.0, vector(element), false)));
    }
    final List<ClipChannel.Key> keys = new ArrayList<>();
    element
        .getAsJsonObject()
        .entrySet()
        .forEach(entry -> keys.add(key(Double.parseDouble(entry.getKey()), entry.getValue())));
    return new ClipChannel(keys);
  }

  private static ClipChannel.Key key(final double time, final JsonElement value) {
    if (value.isJsonArray()) {
      return new ClipChannel.Key(time, vector(value), false);
    }
    final JsonObject frame = value.getAsJsonObject();
    final boolean smooth =
        frame.has("lerp_mode") && CATMULL_ROM.equals(frame.get("lerp_mode").getAsString());
    return new ClipChannel.Key(
        time, vector(frame.has("post") ? frame.get("post") : frame.get("pre")), smooth);
  }

  static Vec3d vector(final JsonElement element) {
    final JsonArray array = element.getAsJsonArray();
    if (array.size() != 3) {
      throw new JsonParseException("A keyframe needs three values: " + array);
    }
    return new Vec3d(number(array.get(0)), number(array.get(1)), number(array.get(2)));
  }

  private static double number(final JsonElement element) {
    try {
      return Double.parseDouble(element.getAsString().trim());
    } catch (final NumberFormatException e) {
      throw new JsonParseException("Only plain numbers are supported, not " + element, e);
    }
  }
}
