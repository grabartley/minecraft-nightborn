package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PrayerRulesTest {

  @ParameterizedTest(name = "{0} health, boost {1} -> {2}")
  @CsvSource({"6, 1.0, 6", "6, 1.25, 7.5", "6, 1.95, 11.7", "2, 0.5, 2", "-1, 2.0, 0"})
  @DisplayName("prayer healing grows with the healer's boost and never shrinks below its base")
  void boosted(final float health, final double boost, final float expected) {
    assertEquals(expected, PrayerRules.boosted(health, boost), 1.0E-4);
  }

  @ParameterizedTest(name = "distance² {0}, radius {1} -> {2}")
  @CsvSource({"0, 6, true", "36, 6, true", "36.1, 6, false"})
  @DisplayName("a prayer reaches its radius and no further")
  void inside(final double distance, final float radius, final boolean inside) {
    assertEquals(inside, PrayerRules.inside(distance, radius));
  }

  @ParameterizedTest(name = "({0}, {1}) from the centre -> {2}")
  @CsvSource({"0, 0, 0", "3, 4, 25", "-2, 0, 4"})
  @DisplayName("a pillar measures only across the ground, whatever the height")
  void flat(final double x, final double z, final double expected) {
    assertEquals(expected, PrayerRules.flat(new Vec3d(x, 40.0, z), Vec3d.ZERO));
  }

  @ParameterizedTest(name = "started {0}, now {1} -> {2}")
  @CsvSource({"100, 100, false", "100, 119, false", "100, 120, true", "100, 140, true"})
  @DisplayName("a lasting prayer pulses once a second after it starts")
  void pulses(final long started, final long now, final boolean pulses) {
    assertEquals(pulses, PrayerRules.pulses(started, now));
  }

  @ParameterizedTest(name = "now {0}, until {1} -> {2}")
  @CsvSource({"0, 200, true", "199, 200, true", "200, 200, false"})
  @DisplayName("a lasting prayer ends on time")
  void lasts(final long now, final long until, final boolean lasts) {
    assertEquals(lasts, PrayerRules.lasts(now, until));
  }

  @ParameterizedTest(name = "{0} healed -> {1} counted")
  @CsvSource({"0, 0", "0.4, 0", "0.6, 1", "7.5, 8", "-1, 0"})
  @DisplayName("healing counts toward a trial in whole points")
  void counted(final float healed, final int counted) {
    assertEquals(counted, PrayerRules.counted(healed));
  }
}
