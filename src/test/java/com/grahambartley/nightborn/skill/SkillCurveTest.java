package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillCurveTest {

  @ParameterizedTest(name = "point {0} at {1} rising {2}%: {3}")
  @CsvSource({
    "0, 100, 3, 100",
    "1, 100, 3, 103",
    "2, 100, 3, 106",
    "50, 100, 3, 438",
    "100, 100, 3, 1922",
    "5, 100, 0, 100",
    "0, 0, 3, 1",
    "-4, 100, 3, 100",
    "3, 100, -5, 100",
    "100000, 100, 3, 1000000000"
  })
  @DisplayName("the first point costs the set amount, each later one more, up to the cap")
  void cost(final int earned, final int first, final int rise, final int cost) {
    assertEquals(cost, SkillCurve.cost(earned, first, rise));
  }

  @ParameterizedTest(name = "{0} points and {1} banked gain {2}: {3} points, {4} banked")
  @CsvSource({
    "0, 0, 99, 0, 99",
    "0, 0, 100, 1, 0",
    "0, 0, 250, 2, 47",
    "1, 50, 53, 2, 0",
    "0, 20, -10, 0, 20",
    "9, 0, 5000, 10, 0",
    "10, 30, 500, 10, 0"
  })
  @DisplayName("gained experience pays out whole points and stops at the most points allowed")
  void add(
      final int earned,
      final float banked,
      final float gained,
      final int points,
      final float left) {
    final SkillCurve.Gain gain = SkillCurve.add(earned, banked, gained, 100, 3, 10);
    assertEquals(points, gain.earned());
    assertEquals(left, gain.experience(), 0.01F);
  }

  @ParameterizedTest(name = "{1} banked at {0} points: {2}")
  @CsvSource({"0, 0, 0", "0, 50, 0.5", "0, 100, 1", "0, 400, 1", "1, 51.5, 0.5", "0, -3, 0"})
  @DisplayName("the bar shows how far the banked experience is toward the next point")
  void fraction(final int earned, final float banked, final float fraction) {
    assertEquals(fraction, SkillCurve.fraction(earned, banked, 100, 3), 0.0001F);
  }
}
