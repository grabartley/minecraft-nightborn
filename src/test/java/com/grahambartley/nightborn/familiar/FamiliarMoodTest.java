package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class FamiliarMoodTest {

  @ParameterizedTest(name = "loyalty {0}, turned {1}: {2}")
  @CsvSource({
    "100, false, DEVOTED",
    "75, false, DEVOTED",
    "74, false, CONTENT",
    "50, false, CONTENT",
    "49, false, UNEASY",
    "25, false, UNEASY",
    "24, false, RESENTFUL",
    "0, false, RESENTFUL",
    "0, true, CONTENT",
    "100, true, CONTENT"
  })
  @DisplayName("a mood shows the band of loyalty, and a turned familiar always looks content")
  void mood(final int loyalty, final boolean turned, final FamiliarMood mood) {
    assertEquals(mood, FamiliarMood.of(loyalty, turned));
  }

  @ParameterizedTest
  @EnumSource(FamiliarMood.class)
  @DisplayName("each mood has its own lang key")
  void key(final FamiliarMood mood) {
    assertEquals(
        "nightborn.familiar.mood." + mood.name().toLowerCase(Locale.ROOT), mood.translationKey());
  }
}
