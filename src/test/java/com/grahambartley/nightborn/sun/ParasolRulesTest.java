package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParasolRulesTest {

  @ParameterizedTest(
      name = "sprinting {0}, levitating {1}, bat form {2}, lying down {3}: closes {4}")
  @CsvSource({
    "false, false, false, false, false",
    "true, false, false, false, true",
    "false, true, false, false, true",
    "false, false, true, false, true",
    "false, false, false, true, true",
    "true, true, true, true, true"
  })
  @DisplayName("sprinting, levitating, taking bat form or lying down closes a parasol")
  void closes(
      final boolean sprinting,
      final boolean levitating,
      final boolean bat,
      final boolean lying,
      final boolean shut) {
    assertEquals(shut, ParasolRules.closes(sprinting, levitating, bat, lying));
  }

  @ParameterizedTest(name = "open {0}, closing {1}: {2}")
  @CsvSource({
    "false, false, OPEN",
    "false, true, REFUSE",
    "true, false, CLOSE",
    "true, true, CLOSE"
  })
  @DisplayName("using a parasol opens it, closes it, or refuses while something would close it")
  void use(final boolean open, final boolean closing, final ParasolRules.Use use) {
    assertEquals(use, ParasolRules.use(open, closing));
  }

  @ParameterizedTest(name = "open {0}, closing {1}: shades {2}")
  @CsvSource({
    "true, false, true",
    "false, false, false",
    "true, true, false",
    "false, true, false"
  })
  @DisplayName("only an open parasol that nothing is closing gives shade")
  void shades(final boolean open, final boolean closing, final boolean shade) {
    assertEquals(shade, ParasolRules.shades(open, closing));
  }

  @ParameterizedTest(name = "struck by someone {0}, damage {1}, blocked {2}: blow {3}")
  @CsvSource({
    "true, 2.0, false, true",
    "true, 0.5, false, true",
    "false, 2.0, false, false",
    "true, 0.0, false, false",
    "true, 2.0, true, false"
  })
  @DisplayName("a blow is a hit from someone that lands")
  void blow(final boolean struck, final float damage, final boolean blocked, final boolean isBlow) {
    assertEquals(isBlow, ParasolRules.blow(struck, damage, blocked));
  }
}
