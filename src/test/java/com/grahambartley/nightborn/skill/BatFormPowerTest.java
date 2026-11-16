package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BatFormPowerTest {

  @Test
  @DisplayName("bat form reads what forces it back")
  void parsesForcedBy() {
    assertEquals(
        List.of("garlic_touch", "holy_water"),
        BatFormPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"land_range\":3.0,\"forced_by\":[\"garlic_touch\",\"holy_water\"]}"))
            .getOrThrow()
            .forcedBy());
  }

  @Test
  @DisplayName("bat form reads how close a vampire mob flies before turning back")
  void parses() {
    assertEquals(
        new BatFormPower(3.0F, List.of()),
        BatFormPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"land_range\":3.0}"))
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"{}", "{\"land_range\":0.5}", "{\"land_range\":65}"})
  @DisplayName("a missing or out of range landing distance is rejected")
  void rejects(final String json) {
    assertFalse(
        BatFormPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "form {0}, target alive {1}, {2} away -> chases {3}")
  @CsvSource({
    "true, true, 20.0, true",
    "true, true, 3.0, false",
    "true, false, 20.0, false",
    "false, true, 20.0, false"
  })
  @DisplayName(
      "a mob bat chases a live target until it is in landing range, and only if it owns the form")
  void chases(
      final boolean owns, final boolean alive, final double distance, final boolean expected) {
    assertEquals(
        expected,
        BatFormPower.chases(
            owns ? Optional.of(new BatFormPower(3.0F, List.of())) : Optional.empty(),
            alive,
            distance));
  }

  @ParameterizedTest(name = "{0} blocks away with range {1} -> {2}")
  @CsvSource({"2.0, 3.0, true", "3.0, 3.0, true", "3.1, 3.0, false", "20.0, 3.0, false"})
  @DisplayName("a mob turns back once it is within its landing range")
  void landsAt(final double distance, final float range, final boolean expected) {
    assertEquals(expected, new BatFormPower(range, List.of()).landsAt(distance));
  }
}
