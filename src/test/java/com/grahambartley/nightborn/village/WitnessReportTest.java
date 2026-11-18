package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WitnessReportTest {

  private static final UUID WITNESS = UUID.randomUUID();
  private static final UUID VAMPIRE = UUID.randomUUID();

  private static WitnessReport report(final SuspicionCause cause, final long night) {
    return new WitnessReport(WITNESS, VAMPIRE, cause, BlockPos.ORIGIN, 1000L, night);
  }

  @ParameterizedTest(name = "alive {0}, {1} blocks² from the bell, {2} ticks on: {3}")
  @CsvSource({
    "true, 400, 0, RUNNING",
    "true, 400, 199, RUNNING",
    "true, 400, 200, TOLD",
    "true, 9, 20, TOLD",
    "true, 10, 20, RUNNING",
    "false, 400, 200, SILENCED",
    "false, 0, 20, SILENCED"
  })
  @DisplayName("a sighting counts once the witness reaches the bell or lives ten seconds")
  void countsWhenTold(
      final boolean alive,
      final double squaredDistance,
      final long ticks,
      final WitnessReport.Outcome outcome) {
    assertEquals(
        outcome,
        report(SuspicionCause.SEEN_FEEDING).outcome(alive, squaredDistance, 1000L + ticks, 200));
  }

  @ParameterizedTest(name = "{0} on night {1} matches feeding on night 4: {2}")
  @CsvSource({"SEEN_FEEDING, 4, true", "SEEN_BURNING, 4, false", "SEEN_FEEDING, 5, false"})
  @DisplayName("a witness tells of each thing a vampire does once a night")
  void onceANight(final SuspicionCause cause, final long night, final boolean same) {
    assertEquals(same, report(SuspicionCause.SEEN_FEEDING, 4).sameSighting(report(cause, night)));
  }

  private static WitnessReport report(final SuspicionCause cause) {
    return report(cause, 0L);
  }

  @ParameterizedTest(name = "{0} by another witness on night {1} at bell {2}: same event {3}")
  @CsvSource({
    "SEEN_FEEDING, 4, 0, true",
    "SEEN_BURNING, 4, 0, false",
    "SEEN_FEEDING, 5, 0, false",
    "SEEN_FEEDING, 4, 9, false"
  })
  @DisplayName("one thing seen by many witnesses counts once for their village")
  void oneEventCountsOnce(
      final SuspicionCause cause, final long night, final int bellX, final boolean same) {
    final WitnessReport other =
        new WitnessReport(UUID.randomUUID(), VAMPIRE, cause, new BlockPos(bellX, 0, 0), 9L, night);
    assertEquals(same, report(SuspicionCause.SEEN_FEEDING, 4).sameEvent(other));
  }
}
