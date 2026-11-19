package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class VampireBuildTest {

  @ParameterizedTest(name = "{0}..{1}, roll {2}: {3}")
  @CsvSource({
    "1.5, 2.0, 0.0, 1.5",
    "1.5, 2.0, 0.5, 1.75",
    "1.5, 2.0, 1.0, 2.0",
    "1.5, 2.0, 2.0, 2.0",
    "2.0, 1.5, 0.0, 1.5",
    "1.8, 1.8, 0.7, 1.8"
  })
  @DisplayName("a height is rolled between the build's lowest and highest")
  void height(final float min, final float max, final double roll, final float height) {
    assertEquals(height, new VampireBuild(1.0F, min, max, 1).height(roll), 1.0E-5F);
  }

  @ParameterizedTest(name = "{0}..{1}: upside down {2}")
  @CsvSource({"1.5, 2.0, false", "1.8, 1.8, false", "2.0, 1.5, true"})
  @DisplayName("a build whose lowest height is above its highest is upside down")
  void upsideDown(final float min, final float max, final boolean upsideDown) {
    assertEquals(upsideDown, new VampireBuild(1.0F, min, max, 1).upsideDown());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"width\":0.88,\"min_height\":1.7,\"max_height\":2.0}",
        "{\"width\":1.14,\"min_height\":1.5,\"max_height\":1.85,\"weight\":3}"
      })
  @DisplayName("a build reads from data with a weight of 1 unless it says otherwise")
  void reads(final String json) {
    final VampireBuild build =
        VampireBuild.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(json.contains("weight") ? 3 : 1, build.weight());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"width\":0.2,\"min_height\":1.7,\"max_height\":2.0}",
        "{\"width\":1.0,\"min_height\":0.5,\"max_height\":2.0}",
        "{\"width\":1.0,\"min_height\":1.5,\"max_height\":4.0}",
        "{\"width\":1.0,\"min_height\":1.5}"
      })
  @DisplayName("a build out of range or missing a field is rejected")
  void rejects(final String json) {
    assertFalse(
        VampireBuild.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
