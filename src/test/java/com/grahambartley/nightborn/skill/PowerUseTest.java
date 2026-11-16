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
      name = "power {0}, downed {1}, weakened {2}, cooling {3}, blood {4}/{5} -> {6}")
  @CsvSource({
    "true, false, false, 0, 10, 3, READY",
    "true, false, false, 0, 3, 3, READY",
    "true, false, false, 0, 0, 0, READY",
    "false, false, false, 0, 20, 0, NO_POWER",
    "true, true, false, 0, 20, 3, HELPLESS",
    "true, true, true, 40, 0, 3, HELPLESS",
    "true, false, true, 0, 20, 3, WEAKENED",
    "true, false, false, 1, 20, 3, COOLING",
    "true, false, false, 0, 2, 3, THIRSTY"
  })
  @DisplayName("a power is refused while downed, weakened, cooling or short of blood")
  void checks(
      final boolean hasPower,
      final boolean helpless,
      final boolean weakened,
      final long cooling,
      final int blood,
      final int cost,
      final PowerUse.Verdict expected) {
    assertEquals(expected, PowerUse.check(hasPower, helpless, weakened, cooling, blood, cost));
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
