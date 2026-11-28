package com.grahambartley.nightborn.ammo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.GearSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GarlicCloudTest {

  @ParameterizedTest(name = "radius {0}, {1}s cloud, {2}% for {3}s")
  @CsvSource({"3, 10, 60, 5, 200, 100", "1, 1, 0, 1, 20, 20", "8, 60, 100, 60, 1200, 1200"})
  @DisplayName("the cloud takes its size, life and strength from the settings")
  void fromSettings(
      final int radius,
      final int cloudSeconds,
      final int strength,
      final int weakenSeconds,
      final int durationTicks,
      final int weakenTicks) {
    final GarlicCloud cloud =
        GarlicCloud.of(
            new GearSettings(4, 6, 40, 10, strength, weakenSeconds, radius, cloudSeconds));
    assertEquals(radius, cloud.radius());
    assertEquals(durationTicks, cloud.durationTicks());
    assertEquals(strength, cloud.strength());
    assertEquals(weakenTicks, cloud.weakenTicks());
  }

  @ParameterizedTest(name = "radius {0} over {1}s")
  @CsvSource({"3, 10", "1, 1", "8, 60"})
  @DisplayName("the cloud thins to half its radius by the time it fades")
  void thinsToHalf(final int radius, final int cloudSeconds) {
    final GarlicCloud cloud =
        GarlicCloud.of(new GearSettings(4, 6, 40, 10, 60, 5, radius, cloudSeconds));
    assertEquals(
        radius * GarlicCloud.SHRINK_TO,
        cloud.radius() + cloud.radiusGrowth() * cloud.durationTicks(),
        1.0e-4);
  }
}
