package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClipFileTest {

  private static final String FILE =
      """
      {"format_version": "1.8.0", "animations": {
        "float": {"loop": true, "animation_length": 2.0, "bones": {
          "root": {"rotation": {"0.0": {"post": ["-16", "0", "0.6"], "lerp_mode": "catmullrom"},
                                "1.0": {"post": ["-17", "0", "-0.6"], "lerp_mode": "catmullrom"},
                                "2.0": {"post": ["-16", "0", "0.6"], "lerp_mode": "catmullrom"}},
                   "position": {"0.0": ["0", "0.4", "0"], "1.0": [0, 0, 0]}},
          "head": {"rotation": ["12", "0", "0"]}}},
        "land": {"loop": "hold_on_last_frame", "animation_length": 0.7, "bones": {
          "body": {"rotation": {"0.3": ["9", "0", "0"]}}}},
        "still": {"animation_length": 1.0}
      }}
      """;

  private static Map<String, AnimationClip> clips() {
    return ClipFile.read(JsonParser.parseString(FILE).getAsJsonObject());
  }

  @ParameterizedTest(name = "{0}: loops {1}, length {2}")
  @CsvSource({"float, true, 2.0", "land, false, 0.7", "still, false, 1.0"})
  @DisplayName("each clip keeps its length and whether it loops")
  void readsClips(final String name, final boolean loops, final double length) {
    final AnimationClip clip = clips().get(name);
    assertEquals(loops, clip.loops());
    assertEquals(length, clip.length(), 1.0E-9);
  }

  @ParameterizedTest(name = "t={0}: root x {1}, lift {2}")
  @CsvSource({"0.0, -16, 0.4", "1.0, -17, 0", "2.0, -16, 0.4"})
  @DisplayName(
      "keys written as text, numbers, or before-and-after pairs all read the same, and a loop wraps")
  void readsKeys(final double time, final double rootX, final double lift) {
    final RigPose pose = clips().get("float").sample(time);
    assertEquals(rootX, pose.bone("root").rotation().x, 1.0E-9);
    assertEquals(lift, pose.bone("root").position().y, 1.0E-9);
    assertEquals(12, pose.bone("head").rotation().x, 1.0E-9);
  }

  @Test
  @DisplayName("a clip with no bones is still read")
  void emptyClip() {
    assertTrue(clips().get("still").tracks().isEmpty());
    assertFalse(clips().get("still").loops());
  }

  @Test
  @DisplayName("an expression in place of a number is refused rather than read as zero")
  void refusesExpressions() {
    final JsonObject file =
        JsonParser.parseString(
                "{\"animations\": {\"x\": {\"bones\": {\"head\": {\"rotation\":"
                    + " [\"math.sin(q.anim_time)\", 0, 0]}}}}}")
            .getAsJsonObject();
    assertThrows(JsonParseException.class, () -> ClipFile.read(file));
  }
}
