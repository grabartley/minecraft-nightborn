package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NameStyleTest {

  private static final Map<String, NameStyle> STYLES =
      Map.of(
          "vampire", NameStyle.VAMPIRE,
          "eldest", NameStyle.ELDEST,
          "hunter", NameStyle.HUNTER,
          "familiar", NameStyle.FAMILIAR);

  static Stream<Arguments> pairs() {
    return Stream.of(
        Arguments.of("vampire", "eldest", true),
        Arguments.of("vampire", "vampire", true),
        Arguments.of("vampire", "hunter", false),
        Arguments.of("hunter", "familiar", false),
        Arguments.of("familiar", "familiar", true));
  }

  @ParameterizedTest(name = "{0} and {1}: {2}")
  @MethodSource("pairs")
  @DisplayName("mobs that share given-name pools avoid each other's names")
  void kin(final String one, final String other, final boolean kin) {
    assertEquals(kin, STYLES.get(one).kin(STYLES.get(other)));
  }

  static Stream<Arguments> colours() {
    return Stream.of(
        Arguments.of("vampire", Optional.empty()),
        Arguments.of("hunter", Optional.of(TextColor.fromFormatting(Formatting.BLUE))),
        Arguments.of("familiar", Optional.empty()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("colours")
  @DisplayName("hunters are blue, familiars plain and vampires take their stage's colour instead")
  void colour(final String style, final Optional<TextColor> colour) {
    assertEquals(colour, STYLES.get(style).colour());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pairs")
  @DisplayName("only vampire styles carry epithets")
  void epithets(final String style, final String ignored, final boolean ignoredKin) {
    assertEquals(
        style.equals("vampire") || style.equals("eldest"),
        STYLES.get(style).epithets().isPresent());
  }
}
