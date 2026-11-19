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

  @ParameterizedTest(
      name = "sinking {0}, raised {1} ago, hurt {2}, struck {3} ago, moving {4}: {5}")
  @CsvSource({
    "true, 5, true, 0, true, SINK",
    "false, 0, true, 0, true, RISE",
    "false, 39, false, -1, true, RISE",
    "false, 40, true, 0, true, HURT",
    "false, 200, false, 0, true, ATTACK",
    "false, 200, false, 11, false, ATTACK",
    "false, 200, false, 12, true, WALK",
    "false, 200, false, -1, false, IDLE",
    "false, -1, false, -1, false, IDLE"
  })
  void picksThePose(
      final boolean sinking,
      final long sinceRaised,
      final boolean hurt,
      final long sinceStruck,
      final boolean moving,
      final RestlessPose expected) {
    assertEquals(expected, RestlessPose.of(sinking, sinceRaised, hurt, sinceStruck, moving));
  }

  @ParameterizedTest(name = "at {0}, now {1}: {2}")
  @CsvSource({"-1, 500, -1", "100, 100, 0", "100, 140, 40"})
  void countsTicksSinceAMoment(final long at, final long now, final long since) {
    assertEquals(since, RestlessPose.since(at, now));
  }

  @ParameterizedTest(name = "sinking {0}, raised {1} ago: held {2}")
  @CsvSource({"true, 500, true", "false, 10, true", "false, 40, false", "false, -1, false"})
  void holdsStillWhileRisingOrSinking(
      final boolean sinking, final long sinceRaised, final boolean held) {
    assertEquals(held, RestlessPose.held(sinking, sinceRaised));
  }

  @ParameterizedTest(name = "{0}: blends {1} ticks")
  @CsvSource({"RISE, 0", "SINK, 4", "WALK, 4"})
  void risesWithoutBlendingFromAStandingPose(final RestlessPose pose, final int blend) {
    assertEquals(blend, pose.blendTicks(4));
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
