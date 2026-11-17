package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class NameRulesTest {

  static Stream<Arguments> typed() {
    return Stream.of(
        Arguments.of("Hester", Optional.of("Hester"), Optional.empty()),
        Arguments.of("  Hester  ", Optional.of("Hester"), Optional.empty()),
        Arguments.of("Old Tobias", Optional.of("Old Tobias"), Optional.empty()),
        Arguments.of("§cRed§r Wren", Optional.of("Red Wren"), Optional.empty()),
        Arguments.of("Ab\u0000\u0007c​d", Optional.of("Abcd"), Optional.empty()),
        Arguments.of("Ma‮rk", Optional.of("Mark"), Optional.empty()),
        Arguments.of("", Optional.empty(), Optional.of(NameRules.Problem.EMPTY)),
        Arguments.of("     ", Optional.empty(), Optional.of(NameRules.Problem.EMPTY)),
        Arguments.of("\u0001\u0002", Optional.empty(), Optional.of(NameRules.Problem.EMPTY)),
        Arguments.of("§l", Optional.empty(), Optional.of(NameRules.Problem.EMPTY)),
        Arguments.of("a".repeat(32), Optional.of("a".repeat(32)), Optional.empty()),
        Arguments.of(" " + "a".repeat(32) + " ", Optional.of("a".repeat(32)), Optional.empty()),
        Arguments.of("a".repeat(33), Optional.empty(), Optional.of(NameRules.Problem.TOO_LONG)),
        Arguments.of("Пётр", Optional.of("Пётр"), Optional.empty()),
        Arguments.of("小明", Optional.of("小明"), Optional.empty()));
  }

  @ParameterizedTest(name = "\"{0}\" -> {1} {2}")
  @MethodSource("typed")
  @DisplayName("a typed name is trimmed, stripped of control and formatting, and kept to 1 to 32")
  void check(
      final String typed, final Optional<String> name, final Optional<NameRules.Problem> problem) {
    final NameRules.Verdict verdict = NameRules.check(typed);
    assertEquals(name, verdict.name());
    assertEquals(problem, verdict.problem());
    assertEquals(name.isPresent(), verdict.ok());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(NameRules.Problem.class)
  @DisplayName("every problem has its own translation key")
  void keys(final NameRules.Problem problem) {
    assertEquals(
        "nightborn.naming.problem." + problem.name().toLowerCase(java.util.Locale.ROOT),
        problem.translationKey());
  }
}
