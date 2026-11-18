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
          "power {0}, downed {1}, held {2}, winged {3}, bat {4}, weakened {5}, sunlit {6},"
              + " cooling {7}, blood {8}/{9}, unseen {10}, nowhere {11}, no subject {12} -> {13}")
  @CsvSource({
    "true, false, false, false, false, false, false, 0, 10, 3, false, false, false, READY",
    "true, false, false, false, false, false, false, 0, 3, 3, false, false, false, READY",
    "true, false, false, false, false, false, false, 0, 0, 0, false, false, false, READY",
    "false, false, false, false, false, false, false, 0, 20, 0, false, false, false, NO_POWER",
    "true, true, false, false, false, false, false, 0, 20, 3, false, false, false, HELPLESS",
    "true, true, true, true, false, true, false, 40, 0, 3, true, false, false, HELPLESS",
    "true, false, true, false, false, false, false, 0, 20, 3, false, false, false, HELD",
    "true, false, true, true, true, true, false, 5, 0, 3, true, true, true, HELD",
    "true, false, false, true, false, false, false, 0, 20, 3, false, false, false, IN_WINGED_FORM",
    "true, false, false, true, true, false, false, 0, 20, 3, false, false, false, IN_WINGED_FORM",
    "true, false, false, false, false, true, false, 0, 20, 3, false, false, false, WEAKENED",
    "true, false, false, false, false, false, false, 1, 20, 3, false, false, false, COOLING",
    "true, false, false, false, false, false, false, 1, 20, 3, true, false, false, COOLING",
    "true, false, false, false, false, false, false, 0, 2, 3, false, false, false, THIRSTY",
    "true, false, false, false, false, false, false, 0, 2, 3, true, false, false, THIRSTY",
    "true, false, false, false, true, false, false, 0, 20, 3, false, false, false, AS_A_BAT",
    "true, false, false, false, true, true, false, 1, 0, 3, false, false, false, AS_A_BAT",
    "true, false, false, false, true, false, false, 0, 20, 3, true, true, false, AS_A_BAT",
    "true, true, false, false, true, false, false, 0, 20, 3, false, false, false, HELPLESS",
    "true, false, false, false, false, false, true, 0, 20, 3, false, false, false, SUNLIT",
    "true, false, false, false, false, true, true, 0, 20, 3, false, false, false, WEAKENED",
    "true, false, false, false, false, false, true, 5, 20, 3, false, false, false, SUNLIT",
    "true, false, false, false, false, false, true, 0, 20, 3, true, true, false, SUNLIT",
    "true, false, false, false, false, false, false, 0, 20, 3, true, false, false, UNSEEN",
    "true, false, false, false, false, false, false, 0, 20, 3, true, true, true, UNSEEN",
    "true, false, false, false, false, false, false, 0, 20, 3, false, true, false, NOWHERE",
    "true, false, false, false, false, false, false, 0, 2, 3, false, true, false, THIRSTY",
    "true, false, false, false, false, false, false, 0, 20, 3, false, false, true, NO_SUBJECT",
    "true, false, false, false, false, false, false, 0, 20, 3, false, true, true, NOWHERE"
  })
  @DisplayName(
      "a power is refused while downed, held, in winged form, in bat form, weakened, in the sun,"
          + " cooling, short of blood, with nothing to aim at, nowhere to step or no one to order")
  void checks(
      final boolean hasPower,
      final boolean helpless,
      final boolean held,
      final boolean winged,
      final boolean bat,
      final boolean weakened,
      final boolean sunlit,
      final long cooling,
      final int blood,
      final int cost,
      final boolean unseen,
      final boolean nowhere,
      final boolean subjectless,
      final PowerUse.Verdict expected) {
    assertEquals(
        expected,
        PowerUse.check(
            new PowerUse.Situation(
                hasPower,
                helpless,
                held,
                winged,
                bat,
                weakened,
                sunlit,
                cooling,
                blood,
                cost,
                unseen,
                nowhere,
                subjectless)));
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
