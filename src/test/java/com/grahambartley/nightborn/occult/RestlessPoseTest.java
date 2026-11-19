package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class RestlessPoseTest {

  private static final Path CLIPS =
      Path.of(
          "src/client/resources/assets/nightborn/animations/entity/restless_dead.animation.json");

  private static JsonObject clips() throws IOException {
    return JsonParser.parseString(Files.readString(CLIPS))
        .getAsJsonObject()
        .getAsJsonObject("animations");
  }

  @ParameterizedTest(name = "sinking {0}, raised {1} ago, hurt {2}, swinging {3}, moving {4}: {5}")
  @CsvSource({
    "true, 5, true, true, true, SINK",
    "false, 0, true, true, true, RISE",
    "false, 39, false, false, true, RISE",
    "false, 40, true, true, true, HURT",
    "false, 200, false, true, true, ATTACK",
    "false, 200, false, false, true, WALK",
    "false, 200, false, false, false, IDLE",
    "false, -1, false, false, false, IDLE"
  })
  void picksThePose(
      final boolean sinking,
      final long sinceRaised,
      final boolean hurt,
      final boolean swinging,
      final boolean moving,
      final RestlessPose expected) {
    assertEquals(expected, RestlessPose.of(sinking, sinceRaised, hurt, swinging, moving));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(RestlessPose.class)
  void everyPoseHasAClip(final RestlessPose pose) throws IOException {
    assertTrue(clips().has(pose.animation()), pose.animation());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = RestlessPose.class,
      names = {"RISE", "SINK"})
  void heldClipsLastAsLongAsTheEntityHoldsStill(final RestlessPose pose) throws IOException {
    final int ticks = pose == RestlessPose.RISE ? RestlessPose.RISE_TICKS : RestlessPose.SINK_TICKS;
    assertEquals(
        ticks / 20.0,
        clips().getAsJsonObject(pose.animation()).get("animation_length").getAsDouble(),
        1.0e-6);
  }

  @ParameterizedTest(name = "rise holds for {0} ticks: {1}")
  @CsvSource({"-1, false", "0, true", "39, true", "40, false"})
  void risingCoversTheRiseClip(final long sinceRaised, final boolean rising) {
    assertEquals(rising, RestlessPose.rising(sinceRaised));
  }
}
