package com.grahambartley.nightborn.skill;

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
import org.junit.jupiter.params.provider.EnumSource;

class PowerUseTest {

  private static final Path ENGLISH =
      Path.of("src/main/resources/assets/nightborn/lang/en_us.json");

  @ParameterizedTest(
      name =
          "power {0}, downed {1}, bat {2}, weakened {3}, sunlit {4}, cooling {5}, blood {6}/{7}"
              + " -> {8}")
  @CsvSource({
    "true, false, false, false, false, 0, 10, 3, READY",
    "true, false, false, false, false, 0, 3, 3, READY",
    "true, false, false, false, false, 0, 0, 0, READY",
    "false, false, false, false, false, 0, 20, 0, NO_POWER",
    "true, true, false, false, false, 0, 20, 3, HELPLESS",
    "true, true, false, true, false, 40, 0, 3, HELPLESS",
    "true, false, false, true, false, 0, 20, 3, WEAKENED",
    "true, false, false, false, false, 1, 20, 3, COOLING",
    "true, false, false, false, false, 0, 2, 3, THIRSTY",
    "true, false, true, false, false, 0, 20, 3, AS_A_BAT",
    "true, false, true, true, false, 1, 0, 3, AS_A_BAT",
    "true, true, true, false, false, 0, 20, 3, HELPLESS",
    "true, false, false, false, true, 0, 20, 3, SUNLIT",
    "true, false, false, true, true, 0, 20, 3, WEAKENED",
    "true, false, false, false, true, 5, 20, 3, SUNLIT"
  })
  @DisplayName(
      "a power is refused while downed, in bat form, weakened, in the sun, cooling or short of"
          + " blood")
  void checks(
      final boolean hasPower,
      final boolean helpless,
      final boolean bat,
      final boolean weakened,
      final boolean sunlit,
      final long cooling,
      final int blood,
      final int cost,
      final PowerUse.Verdict expected) {
    assertEquals(
        expected, PowerUse.check(hasPower, helpless, bat, weakened, sunlit, cooling, blood, cost));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = PowerUse.Verdict.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"READY", "NO_POWER"})
  @DisplayName("every refusal the action bar shows has an English line")
  void everyVerdictIsWorded(final PowerUse.Verdict verdict) throws IOException {
    final JsonObject english =
        JsonParser.parseString(Files.readString(ENGLISH, StandardCharsets.UTF_8)).getAsJsonObject();
    assertTrue(english.has(verdict.translationKey()), verdict.translationKey());
  }
}
