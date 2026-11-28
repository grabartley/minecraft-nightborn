package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class IntentTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Intent.class)
  @DisplayName("each intent is found by its data name")
  void byKey(final Intent intent) {
    assertEquals(Optional.of(intent), Intent.byKey(intent.key()));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"close_in, CLOSE_IN", "fight, FIGHT", "FIGHT, ", "flee, "})
  @DisplayName("an intent's data name is its lower case name, and nothing else matches")
  void names(final String key, final Intent intent) {
    assertEquals(Optional.ofNullable(intent), Intent.byKey(key));
  }

  @ParameterizedTest(name = "{0}: fights {1}")
  @CsvSource({
    "FIGHT, true",
    "CLOSE_IN, true",
    "RETREAT, false",
    "HUNT, false",
    "TRAVEL, false",
    "GUARD, false",
    "REST, false"
  })
  @DisplayName("only fighting and closing in are fighting intents")
  void fights(final Intent intent, final boolean fights) {
    assertEquals(fights, intent.fights());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"close_in, CLOSE_IN", "flee, "})
  @DisplayName("data reads an intent by its data name and refuses any other")
  void codec(final String key, final Intent expected) {
    assertEquals(
        Optional.ofNullable(expected),
        Intent.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(key)).result());
  }
}
