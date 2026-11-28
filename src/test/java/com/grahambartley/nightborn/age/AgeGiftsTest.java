package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.NightbornConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeGiftsTest {

  @ParameterizedTest(name = "rank {0} -> {1} blood")
  @CsvSource({"-1, 0", "0, 0", "1, 2", "2, 4", "3, 6"})
  @DisplayName("each stage above fledgling makes the blood bar larger by the same amount")
  void bloodBar(final int rank, final int blood) {
    assertEquals(blood, AgeGifts.bloodBar(rank, NightbornConfig.DEFAULTS.age()));
  }

  @ParameterizedTest(name = "{0} s, rank {1}, {2}% -> {3} s")
  @CsvSource({
    "300, 0, 10, 300",
    "300, 1, 10, 333",
    "300, 2, 10, 375",
    "300, 3, 10, 428",
    "300, 3, 0, 300",
    "300, 3, 30, 3000",
    "300, 9, 30, 3000",
    "300, -2, 10, 300"
  })
  @DisplayName("blood drains slower with each stage and never stops")
  void drainsSlower(final int seconds, final int rank, final int percent, final int slowed) {
    assertEquals(slowed, AgeGifts.drainSeconds(seconds, rank, percent));
  }
}
