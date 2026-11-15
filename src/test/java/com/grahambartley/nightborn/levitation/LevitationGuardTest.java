package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalDouble;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationGuardTest {

  @ParameterizedTest(name = "strikes {0}, lowest {1}, feet {2}, surface {3}: strikes {4}")
  @CsvSource({
    "0, NaN, 65.0, 64, 0",
    "0, NaN, 65.7, 64, 0",
    "0, NaN, 65.8, 64, 1",
    "5, 66.0, 66.0, 64, 6",
    "5, 66.0, 66.2, 64, 6",
    "5, 66.0, 65.95, 64, 5",
    "5, 66.0, 66.0, , 6",
    "5, 66.0, 65.9, , 5",
    "12, 70.0, 65.0, 64, 0",
    "5, 65.9, 66.0, 64, 6"
  })
  @DisplayName("strikes build while held too high, pause while coming down, and clear at the hover")
  void strikes(
      final int strikes,
      final double lowest,
      final double feet,
      final Double surface,
      final int after) {
    final OptionalDouble below =
        surface == null ? OptionalDouble.empty() : OptionalDouble.of(surface);
    assertEquals(after, new LevitationGuard(strikes, lowest).next(feet, below).strikes());
  }

  @ParameterizedTest(name = "{0} strikes: lands {1}")
  @CsvSource({"0, false", "39, false", "40, true", "90, true"})
  @DisplayName("two seconds held too high lands the vampire")
  void lands(final int strikes, final boolean lands) {
    assertEquals(lands, new LevitationGuard(strikes, 0.0).lands());
  }

  @ParameterizedTest(name = "{0} ticks held at {1} over 64")
  @CsvSource({"40, 66.0, true", "39, 66.0, false", "200, 65.0, false"})
  @DisplayName("a client that holds a player up gets landed, one that follows the lift does not")
  void heldUp(final int ticks, final double feet, final boolean lands) {
    LevitationGuard guard = LevitationGuard.FRESH;
    for (int tick = 0; tick < ticks; tick++) {
      guard = guard.next(feet, OptionalDouble.of(64.0));
    }
    assertEquals(lands, guard.lands());
  }

  @ParameterizedTest(name = "bobbing between {0} and {1} for 42 ticks: lands {2}")
  @CsvSource({"66.0, 65.92, true", "66.0, 66.08, true", "66.0, 65.0, false"})
  @DisplayName("bobbing up and down while held too high still lands the vampire")
  void bobbing(final double high, final double low, final boolean lands) {
    LevitationGuard guard = LevitationGuard.FRESH;
    for (int tick = 0; tick < 42; tick++) {
      guard = guard.next(tick % 2 == 0 ? high : low, OptionalDouble.of(64.0));
    }
    assertEquals(lands, guard.lands());
  }
}
