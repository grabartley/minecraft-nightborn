package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class DayJobTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "GATHER, nightborn.job.gather, nightborn:wheel/gather",
    "FETCH_BLOOD, nightborn.job.fetch_blood, nightborn:wheel/fetch_blood",
    "SCOUT, nightborn.job.scout, nightborn:wheel/scout",
    "STAND_WATCH, nightborn.job.stand_watch, nightborn:wheel/stand_watch"
  })
  @DisplayName("each day job has its own label, order message and icon")
  void wheel(final DayJob job, final String key, final String icon) {
    assertEquals(key, job.translationKey());
    assertEquals(key + ".ordered", job.orderedKey());
    assertEquals(Identifier.of(icon), job.icon());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "0, GATHER",
    "1, FETCH_BLOOD",
    "2, SCOUT",
    "3, STAND_WATCH",
    "-1, GATHER",
    "7, GATHER"
  })
  @DisplayName("a wheel argument maps to its job, and anything else to gathering")
  void byIndex(final int index, final DayJob job) {
    assertEquals(job, DayJob.byIndex(index));
  }

  @ParameterizedTest
  @EnumSource(DayJob.class)
  @DisplayName("a job survives a save and load")
  void codec(final DayJob job) {
    assertEquals(
        job,
        DayJob.CODEC
            .parse(NbtOps.INSTANCE, DayJob.CODEC.encodeStart(NbtOps.INSTANCE, job).getOrThrow())
            .getOrThrow());
  }
}
