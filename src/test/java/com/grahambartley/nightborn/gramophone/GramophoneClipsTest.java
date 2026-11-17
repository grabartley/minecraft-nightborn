package com.grahambartley.nightborn.gramophone;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class GramophoneClipsTest {

  @ParameterizedTest(name = "first seen {0}: holds {1}")
  @CsvSource({"EMPTY, idle", "LOADED, loaded", "PLAYING, playing"})
  @DisplayName("a gramophone first seen holds its state without replaying how it got there")
  void firstSight(final GramophoneState state, final String clip) {
    assertEquals(Optional.of(List.of(clip)), new GramophoneClips().observe(state));
  }

  static Stream<Arguments> changes() {
    return Stream.of(
        Arguments.of(GramophoneState.EMPTY, GramophoneState.PLAYING, "playing"),
        Arguments.of(GramophoneState.EMPTY, GramophoneState.LOADED, "loaded"),
        Arguments.of(GramophoneState.LOADED, GramophoneState.PLAYING, "playing"),
        Arguments.of(GramophoneState.PLAYING, GramophoneState.LOADED, "stop,loaded"),
        Arguments.of(GramophoneState.PLAYING, GramophoneState.EMPTY, "idle"),
        Arguments.of(GramophoneState.LOADED, GramophoneState.EMPTY, "idle"));
  }

  @ParameterizedTest(name = "{0} to {1}: {2}")
  @MethodSource("changes")
  @DisplayName("a disc shows or goes at once, and only a stopping platter winds down first")
  void change(final GramophoneState before, final GramophoneState now, final String clips) {
    final GramophoneClips seen = new GramophoneClips();
    seen.observe(before);
    assertEquals(Optional.of(Arrays.asList(clips.split(","))), seen.observe(now));
  }

  @ParameterizedTest(name = "{0} seen twice")
  @CsvSource({"EMPTY", "LOADED", "PLAYING"})
  @DisplayName("nothing restarts while the state stays the same")
  void unchanged(final GramophoneState state) {
    final GramophoneClips seen = new GramophoneClips();
    seen.observe(state);
    assertEquals(Optional.empty(), seen.observe(state));
  }
}
