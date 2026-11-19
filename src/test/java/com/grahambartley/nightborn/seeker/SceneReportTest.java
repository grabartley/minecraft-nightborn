package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SceneReportTest {

  private static final Path ENGLISH =
      Path.of("src/main/resources/assets/nightborn/lang/en_us.json");

  @ParameterizedTest(name = "vampire found {0} -> {1}")
  @CsvSource({"true, nightborn.seeker.scene", "false, nightborn.seeker.scene_cold"})
  @DisplayName("a scene points the way a vampire went, or says its trail is cold, in English")
  void key(final boolean found, final String key) throws IOException {
    assertEquals(key, SceneReport.key(found));
    final JsonObject english =
        JsonParser.parseString(Files.readString(ENGLISH, StandardCharsets.UTF_8)).getAsJsonObject();
    assertTrue(english.has(key), key);
  }
}
