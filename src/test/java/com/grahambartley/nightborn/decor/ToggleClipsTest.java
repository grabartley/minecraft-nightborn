package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ToggleClipsTest {

  static Stream<Arguments> sequences() {
    return Stream.of(
        Arguments.of(List.of(false), List.of(Optional.of(List.of("inactive")))),
        Arguments.of(List.of(true), List.of(Optional.of(List.of("active")))),
        Arguments.of(
            List.of(false, false, true),
            List.of(
                Optional.of(List.of("inactive")),
                Optional.empty(),
                Optional.of(List.of("activate", "active")))),
        Arguments.of(
            List.of(true, false, false),
            List.of(
                Optional.of(List.of("active")),
                Optional.of(List.of("deactivate", "inactive")),
                Optional.empty())));
  }

  @ParameterizedTest(name = "{0} plays {1}")
  @MethodSource("sequences")
  void holdsOnFirstSightAndTransitionsOnlyOnChange(
      final List<Boolean> observed, final List<Optional<List<String>>> expected) {
    final ToggleClips clips = new ToggleClips();
    final List<Optional<List<String>>> played = new ArrayList<>();
    observed.forEach(active -> played.add(clips.observe(active)));
    assertEquals(expected, played);
  }
}
