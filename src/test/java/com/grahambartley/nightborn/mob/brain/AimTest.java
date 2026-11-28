package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
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

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"allies, ALLIES", "enemy, "})
  @DisplayName("data reads an aim by its data name and refuses any other")
  void codec(final String key, final Aim expected) {
    assertEquals(
        Optional.ofNullable(expected),
        Aim.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(key)).result());
  }
}
