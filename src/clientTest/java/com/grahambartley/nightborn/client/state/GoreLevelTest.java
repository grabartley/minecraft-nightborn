package com.grahambartley.nightborn.client.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class GoreLevelTest {

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({"FULL, full", "REDUCED, reduced", "OFF, off"})
  @DisplayName("each level has a lower-case id that finds it again")
  void idsRoundTrip(final GoreLevel level, final String id) {
    assertEquals(id, level.id());
    assertEquals(Optional.of(level), GoreLevel.byId(id));
  }

  @ParameterizedTest(name = "\"{0}\"")
  @ValueSource(strings = {"", "FULL", "none", "reduce", " off"})
  @DisplayName("an unknown id finds no level")
  void unknownIds(final String id) {
    assertEquals(Optional.empty(), GoreLevel.byId(id));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(GoreLevel.class)
  @DisplayName("each level has its English name")
  void hasEnglishText(final GoreLevel level) throws IOException {
    final JsonObject english =
        JsonParser.parseString(
                Files.readString(
                    Path.of("src/main/resources/assets/nightborn/lang/en_us.json"),
                    StandardCharsets.UTF_8))
            .getAsJsonObject();
    assertTrue(english.has(level.translationKey()), level.translationKey());
  }
}
