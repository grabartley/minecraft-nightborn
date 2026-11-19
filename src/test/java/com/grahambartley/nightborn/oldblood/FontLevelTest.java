package com.grahambartley.nightborn.oldblood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class FontLevelTest {

  @ParameterizedTest(name = "progress {0} of {1} days: {2}")
  @CsvSource({
    "-1, 7, FULL",
    "0, 7, EMPTY",
    "-50, 7, EMPTY",
    "83999, 7, EMPTY",
    "84000, 7, LOW",
    "167999, 7, LOW",
    "168000, 7, HALF",
    "252000, 7, HIGH",
    "335999, 7, HIGH",
    "999999, 7, HIGH",
    "24000, 1, HALF",
    "47999, 1, HIGH"
  })
  @DisplayName("the font shows a quarter more blood for each quarter of its refill")
  void showsRefillProgress(final long progress, final int days, final FontLevel level) {
    assertEquals(level, FontLevel.of(progress, days));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"EMPTY, empty", "LOW, low", "HALF, half", "HIGH, high", "FULL, full"})
  @DisplayName("each level names its own looping clip and block state value")
  void namesItsClip(final FontLevel level, final String clip) {
    assertEquals(clip, level.clip());
    assertEquals(clip, level.asString());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(FontLevel.class)
  @DisplayName("only a full font can be drunk from")
  void onlyFullIsFull(final FontLevel level) {
    assertEquals(level == FontLevel.FULL, level.full());
  }
}
