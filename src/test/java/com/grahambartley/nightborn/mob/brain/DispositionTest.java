package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DispositionTest {

  private static final Disposition HOT =
      new Disposition(Map.of(Intent.FIGHT, 1.5F), Map.of(Aim.TARGET, 2.0F, Aim.SELF, 0.5F), 0.5F);

  @ParameterizedTest(name = "{0} at {1}: {2}")
  @CsvSource({
    "FIGHT, TARGET, 3",
    "FIGHT, SELF, 0.75",
    "FIGHT, ALLY, 1.5",
    "RETREAT, TARGET, 2",
    "TRAVEL, SPOT, 1"
  })
  @DisplayName("a disposition leans each use by its intent and its aim, and leaves the rest be")
  void lean(final Intent intent, final Aim aim, final double lean) {
    assertEquals(lean, HOT.lean(intent, aim), 1.0E-6);
  }

  @ParameterizedTest(name = "{0} at {1}")
  @CsvSource({"FIGHT, TARGET", "REST, ALLIES"})
  @DisplayName("an even disposition leans no way")
  void even(final Intent intent, final Aim aim) {
    assertEquals(1.0, Disposition.EVEN.lean(intent, aim));
  }

  @Test
  @DisplayName("a disposition with nothing in its data is even")
  void defaults() {
    assertEquals(
        Disposition.EVEN,
        Disposition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @Test
  @DisplayName("a disposition reads its leans by intent and aim name")
  void reads() {
    assertEquals(
        HOT,
        Disposition.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"intents\": {\"fight\": 1.5}, \"aims\": {\"target\": 2, \"self\": 0.5},"
                        + " \"thrift\": 0.5}"))
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"intents\": {\"fight\": 4}} | true",
        "{\"intents\": {\"fight\": 4.5}} | false",
        "{\"intents\": {\"brawl\": 1}} | false",
        "{\"aims\": {\"self\": 0}} | true",
        "{\"aims\": {\"self\": -1}} | false",
        "{\"aims\": {\"sky\": 1}} | false",
        "{\"thrift\": 4} | true",
        "{\"thrift\": 5} | false"
      })
  @DisplayName("a lean out of range, or on an intent or aim that does not exist, is refused")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, Disposition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
