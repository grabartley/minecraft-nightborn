package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class AimTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Aim.class)
  @DisplayName("each aim is found by its data name")
  void byKey(final Aim aim) {
    assertEquals(Optional.of(aim), Aim.byKey(aim.key()));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"allies, ALLIES", "spot, SPOT", "TARGET, ", "enemy, "})
  @DisplayName("an aim's data name is its lower case name, and nothing else matches")
  void names(final String key, final Aim aim) {
    assertEquals(Optional.ofNullable(aim), Aim.byKey(key));
  }
}
