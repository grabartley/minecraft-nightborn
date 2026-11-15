package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrackingDirectionTest {

  @ParameterizedTest(name = "facing {2}, vampire at ({0}, {1}): {3}")
  @CsvSource({
    "0, 10, 0, ↑",
    "-10, 0, 0, →",
    "10, 0, 0, ←",
    "0, -10, 0, ↓",
    "-10, 10, 0, ↗",
    "10, 10, 0, ↖",
    "-10, -10, 0, ↘",
    "10, -10, 0, ↙",
    "0, -10, 180, ↑",
    "10, 0, 90, ↓",
    "0, 10, 90, ←",
    "0, 10, -270, ←",
    "0, 10, 720, ↑"
  })
  @DisplayName("the arrow points at the vampire relative to where the hunter faces")
  void arrow(final double dx, final double dz, final float yaw, final String arrow) {
    assertEquals(arrow, TrackingDirection.arrow(dx, dz, yaw));
  }

  @ParameterizedTest(name = "({0}, {1}) is yaw {2}")
  @CsvSource({"0, 1, 0", "-1, 0, 90", "1, 0, -90", "1, -1, -135", "-1, -1, 135"})
  @DisplayName("the yaw towards a point follows the vanilla compass")
  void yawTowards(final double dx, final double dz, final float yaw) {
    assertEquals(yaw, TrackingDirection.yawTowards(dx, dz), 0.001F);
  }
}
