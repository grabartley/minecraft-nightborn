package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SuspicionCauseTest {

  private static final Path SOURCES =
      Path.of("src/main/resources/data/nightborn/nightborn/suspicion_source");

  @ParameterizedTest(name = "{0} adds {1}")
  @CsvSource({
    "SEEN_FEEDING, 25",
    "SEEN_LEVITATING, 10",
    "SEEN_IN_BAT_FORM, 10",
    "SEEN_USING_A_POWER, 10",
    "SEEN_BURNING, 10",
    "SEEN_BRINGING_UP_FOOD, 10",
    "BITTEN_VILLAGER, 5",
    "DRAINED_ANIMAL, 5",
    "MISSING_VILLAGER, 5",
    "TAKEN_NEIGHBOUR, 1"
  })
  @DisplayName("every cause has a data entry holding its locked default")
  void eachCauseIsData(final SuspicionCause cause, final int amount) throws IOException {
    final Path file = SOURCES.resolve(cause.id().getPath() + ".json");
    final SuspicionSource source =
        SuspicionSource.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
            .getOrThrow();
    assertEquals(amount, source.amount());
    assertEquals("nightborn", cause.id().getNamespace());
  }
}
