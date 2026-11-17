package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NameBearerTest {

  static Stream<NameStyle> styles() {
    return Stream.of(NameStyle.VAMPIRE, NameStyle.HUNTER, NameStyle.FAMILIAR);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("styles")
  @DisplayName("a bearer with nothing of its own has its style's colour, no epithet and no namer")
  void defaults(final NameStyle style) {
    final NameBearer bearer =
        new NameBearer() {
          @Override
          public NameStyle nameStyle() {
            return style;
          }

          @Override
          public boolean bearsWomansName() {
            return false;
          }
        };
    bearer.bearEpithet("the Pale");
    assertEquals(style.colour(), bearer.nameColour());
    assertEquals("", bearer.epithet());
    assertEquals(Optional.empty(), bearer.namedBy());
  }
}
