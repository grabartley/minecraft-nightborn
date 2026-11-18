package com.grahambartley.nightborn.client.moment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CeremonyCameraTest {

  @ParameterizedTest(name = "start {0}, progress {1} -> yaw {2}, pitch {3}, distance {4}")
  @CsvSource({
    "0, 0, 0, 0, 0",
    "0, 0.5, 180, 20, 4",
    "0, 1, 360, 0, 0",
    "90, 0.5, 270, 20, 4",
    "0, -1, 0, 0, 0",
    "0, 2, 360, 0, 0"
  })
  @DisplayName("the view leaves the player, circles them once and returns to them")
  void orbit(
      final float start,
      final float progress,
      final float yaw,
      final float pitch,
      final float distance) {
    final CeremonyCamera.Shot shot = CeremonyCamera.at(start, progress);
    assertEquals(yaw, shot.yaw(), 1.0E-3F);
    assertEquals(pitch, shot.pitch(), 1.0E-3F);
    assertEquals(distance, shot.distance(), 1.0E-3F);
  }
}
