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
  private static final int REPORT_TICKS = 200;

  private static WitnessReport report(final SuspicionCause cause) {
    return new WitnessReport(WITNESS, VAMPIRE, cause, BlockPos.ORIGIN, 1000L);
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
        report(SuspicionCause.SEEN_FEEDING)
            .outcome(alive, squaredDistance, 1000L + ticks, REPORT_TICKS));
  }

  @ParameterizedTest(name = "{0} seen {1} ticks later at bell {2}: same act {3}")
  @CsvSource({
    "SEEN_FEEDING, 0, 0, true",
    "SEEN_FEEDING, 199, 0, true",
    "SEEN_FEEDING, 200, 0, false",
    "SEEN_FEEDING, -150, 0, true",
    "SEEN_BURNING, 0, 0, false",
    "SEEN_FEEDING, 0, 9, false"
  })
  @DisplayName("one act seen by many witnesses counts once, and a later act counts again")
  void oneActCountsOnce(
      final SuspicionCause cause, final long later, final int bellX, final boolean same) {
    final WitnessReport other =
        new WitnessReport(
            UUID.randomUUID(), VAMPIRE, cause, new BlockPos(bellX, 0, 0), 1000L + later);
    assertEquals(same, report(SuspicionCause.SEEN_FEEDING).sameEvent(other, REPORT_TICKS));
  }

  @ParameterizedTest(name = "{0} by the same witness: same sighting {1}")
  @CsvSource({"SEEN_FEEDING, true", "SEEN_LEVITATING, false"})
  @DisplayName(
      "a witness already running for the bell does not start a second run for the same thing")
  void oneRunPerWitness(final SuspicionCause cause, final boolean same) {
    assertEquals(same, report(SuspicionCause.SEEN_FEEDING).sameSighting(report(cause)));
  }

  @ParameterizedTest(name = "{0} ticks on, stale {1}")
  @CsvSource({"399, false", "400, true"})
  void forgetsOldActs(final long ticks, final boolean stale) {
    assertEquals(stale, report(SuspicionCause.SEEN_FEEDING).stale(1000L + ticks, 400));
  }
}
