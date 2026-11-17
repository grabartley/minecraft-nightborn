package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KinNamesTest {

  static Stream<Arguments> kin() {
    return Stream.of(
        Arguments.of(List.of(), Set.of(), Set.of()),
        Arguments.of(
            List.of(new KinNames.Held("Aurelian the Pale", "the Pale")),
            Set.of("Aurelian"),
            Set.of("the Pale")),
        Arguments.of(
            List.of(
                new KinNames.Held("Aurelian the Pale", "the Pale"),
                new KinNames.Held("Isolde the Unwept", "the Unwept"),
                new KinNames.Held("Tobias", "")),
            Set.of("Aurelian", "Isolde", "Tobias"),
            Set.of("the Pale", "the Unwept")),
        Arguments.of(List.of(new KinNames.Held("Old Wren", "")), Set.of("Old Wren"), Set.of()));
  }

  @ParameterizedTest(name = "{index}: {1} {2}")
  @MethodSource("kin")
  @DisplayName("kin hold their given names and epithets, and a renamed mob holds its whole name")
  void taken(final List<KinNames.Held> kin, final Set<String> givens, final Set<String> epithets) {
    final KinNames.Taken taken = KinNames.taken(kin);
    assertEquals(givens, taken.givens());
    assertEquals(epithets, taken.epithets());
  }
}
