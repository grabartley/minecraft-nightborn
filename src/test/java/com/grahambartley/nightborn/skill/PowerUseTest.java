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
          "power {0}, downed {1}, bat {2}, weakened {3}, sunlit {4}, cooling {5}, blood {6}/{7},"
              + " unseen {8}, nowhere {9} -> {10}")
  @CsvSource({
    "true, false, false, false, false, 0, 10, 3, false, false, READY",
    "true, false, false, false, false, 0, 3, 3, false, false, READY",
    "true, false, false, false, false, 0, 0, 0, false, false, READY",
    "false, false, false, false, false, 0, 20, 0, false, false, NO_POWER",
    "true, true, false, false, false, 0, 20, 3, false, false, HELPLESS",
    "true, true, false, true, false, 40, 0, 3, true, false, HELPLESS",
    "true, false, false, true, false, 0, 20, 3, false, false, WEAKENED",
    "true, false, false, false, false, 1, 20, 3, false, false, COOLING",
    "true, false, false, false, false, 1, 20, 3, true, false, COOLING",
    "true, false, false, false, false, 0, 2, 3, false, false, THIRSTY",
    "true, false, false, false, false, 0, 2, 3, true, false, THIRSTY",
    "true, false, true, false, false, 0, 20, 3, false, false, AS_A_BAT",
    "true, false, true, true, false, 1, 0, 3, false, false, AS_A_BAT",
    "true, false, true, false, false, 0, 20, 3, true, true, AS_A_BAT",
    "true, true, true, false, false, 0, 20, 3, false, false, HELPLESS",
    "true, false, false, false, true, 0, 20, 3, false, false, SUNLIT",
    "true, false, false, true, true, 0, 20, 3, false, false, WEAKENED",
    "true, false, false, false, true, 5, 20, 3, false, false, SUNLIT",
    "true, false, false, false, true, 0, 20, 3, true, true, SUNLIT",
    "true, false, false, false, false, 0, 20, 3, true, false, UNSEEN",
    "true, false, false, false, false, 0, 20, 3, true, true, UNSEEN",
    "true, false, false, false, false, 0, 20, 3, false, true, NOWHERE",
    "true, false, false, false, false, 0, 2, 3, false, true, THIRSTY"
  })
  @DisplayName(
      "a power is refused while downed, in bat form, weakened, in the sun, cooling, short of"
          + " blood, with nothing to stare at or nowhere to step")
  void checks(
      final boolean hasPower,
      final boolean helpless,
      final boolean bat,
      final boolean weakened,
      final boolean sunlit,
      final long cooling,
      final int blood,
      final int cost,
      final boolean unseen,
      final boolean nowhere,
      final PowerUse.Verdict expected) {
    assertEquals(
        expected,
        PowerUse.check(
            hasPower, helpless, bat, weakened, sunlit, cooling, blood, cost, unseen, nowhere));
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
