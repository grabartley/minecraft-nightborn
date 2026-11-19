package com.grahambartley.nightborn.oldblood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FontClipsTest {

  @ParameterizedTest(name = "{0} then {1}: {2}")
  @CsvSource({
    "FULL, EMPTY, drink empty",
    "HIGH, FULL, refill full",
    "EMPTY, FULL, refill full",
    "EMPTY, LOW, low",
    "LOW, HALF, half",
    "HALF, HIGH, high"
  })
  @DisplayName("a level change plays the drink or refill clip before the new level's loop")
  void playsTheChange(final FontLevel before, final FontLevel after, final String clips) {
    final FontClips fontClips = new FontClips();
    fontClips.observe(before);
    assertEquals(Optional.of(Arrays.asList(clips.split(" "))), fontClips.observe(after));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"EMPTY", "LOW", "HALF", "HIGH", "FULL"})
  @DisplayName("a font first seen holds its level's loop, then waits for a change")
  void holdsTheFirstLevel(final FontLevel level) {
    final FontClips fontClips = new FontClips();
    assertEquals(Optional.of(List.of(level.clip())), fontClips.observe(level));
    assertEquals(Optional.empty(), fontClips.observe(level));
  }
}
