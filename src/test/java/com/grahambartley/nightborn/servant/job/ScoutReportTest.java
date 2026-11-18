package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScoutReportTest {

  @ParameterizedTest(name = "{0} villagers, {1} hunters, {2} vampires, {3} humans, {4} others")
  @CsvSource({"0, 0, 0, 0, 0", "3, 1, 0, 0, 2", "0, 5, 2, 1, 0", "12, 0, 1, 3, 4"})
  @DisplayName("a report counts every kind it saw and nothing it did not")
  void of(
      final int villagers,
      final int hunters,
      final int vampires,
      final int humans,
      final int others) {
    final List<ScoutSighting> seen = new ArrayList<>();
    seen.addAll(Collections.nCopies(villagers, ScoutSighting.VILLAGER));
    seen.addAll(Collections.nCopies(hunters, ScoutSighting.HUNTER));
    seen.addAll(Collections.nCopies(vampires, ScoutSighting.VAMPIRE));
    seen.addAll(Collections.nCopies(humans, ScoutSighting.HUMAN));
    seen.addAll(Collections.nCopies(others, ScoutSighting.NONE));
    Collections.shuffle(seen);
    assertEquals(new ScoutReport(villagers, hunters, vampires, humans), ScoutReport.of(seen));
  }

  @ParameterizedTest(name = "{0}, {1}, {2}, {3}")
  @CsvSource({"0, 0, 0, 0", "4, 2, 1, 3"})
  @DisplayName("a report survives a save and load")
  void codec(final int villagers, final int hunters, final int vampires, final int humans) {
    final ScoutReport report = new ScoutReport(villagers, hunters, vampires, humans);
    assertEquals(
        report,
        ScoutReport.CODEC
            .parse(
                NbtOps.INSTANCE,
                ScoutReport.CODEC.encodeStart(NbtOps.INSTANCE, report).getOrThrow())
            .getOrThrow());
  }
}
