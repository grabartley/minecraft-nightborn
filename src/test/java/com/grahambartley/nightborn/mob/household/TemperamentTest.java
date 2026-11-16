package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TemperamentTest {

  private static final Path DATA =
      Path.of("src/main/resources/data/nightborn/nightborn/vampire_temperament");

  @ParameterizedTest(name = "{0}: weight {1}, temper {2}, mourns {3}s, favours {4}")
  @CsvSource({
    "brooding, 3, 15, 120, READ",
    "convivial, 3, 10, 60, CONVERSE",
    "quarrelsome, 2, 70, 30, CONVERSE",
    "melancholic, 2, 20, 300, SIT",
    "restless, 2, 35, 45, STROLL"
  })
  @DisplayName("every shipped temperament loads and favours its own hobby")
  void shippedTemperamentsLoad(
      final String id,
      final int weight,
      final int temper,
      final int mourning,
      final Hobby favourite)
      throws IOException {
    final Temperament temperament =
        Temperament.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(Files.readString(DATA.resolve(id + ".json"))))
            .getOrThrow();
    assertEquals(weight, temperament.weight());
    assertEquals(temper, temperament.temper());
    assertEquals(mourning, temperament.mourning());
    final int top =
        temperament.hobbies().values().stream().mapToInt(Integer::intValue).max().orElseThrow();
    assertEquals(top, temperament.hobbies().get(favourite));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"weight\":1,\"hobbies\":{},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0}",
        "{\"weight\":1,\"hobbies\":{\"sit\":0},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0}",
        "{\"weight\":0,\"hobbies\":{\"sit\":1},\"temper\":0,\"pastime_seconds\":30,\"mourning_seconds\":0}",
        "{\"weight\":1,\"hobbies\":{\"sit\":1},\"temper\":101,\"pastime_seconds\":30,\"mourning_seconds\":0}",
        "{\"weight\":1,\"hobbies\":{\"sit\":1},\"temper\":0,\"pastime_seconds\":0,\"mourning_seconds\":0}"
      })
  @DisplayName("a temperament with no weighted hobby or values out of range is refused")
  void refusesBadData(final String json) {
    assertTrue(Temperament.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
