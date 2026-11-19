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
          "power {0}, downed {1}, held {2}, barred {3}, grounded {4}, winged {5}, bat {6},"
              + " weakened {7}, sunlit {8}, cooling {9}, blood {10}/{11}, unseen {12}, nowhere {13},"
              + " no ground {14}, no subject {15}, rebuked {16}, no crucifix {17}, unarmed {18},"
              + " hurt {19}, smothered {20}, no stake {21}, unloaded {22},"
              + " revealed {23}, no scene {24} -> {25}")
  @CsvSource({
    "true, false, false, false, false, false, false, false, false, 0, 10, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, READY",
    "true, false, false, false, false, false, false, false, false, 0, 3, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, READY",
    "true, false, false, false, false, false, false, false, false, 0, 0, 0, false, false, false, false, false, false, false, false, false, false, false, false, false, READY",
    "false, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, false, false, false, false, false, NO_POWER",
    "true, true, false, false, false, false, false, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, HELPLESS",
    "true, true, true, false, false, true, false, true, false, 40, 0, 3, true, false, false, false, false, false, false, false, false, false, false, false, false, HELPLESS",
    "true, false, true, false, false, false, false, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, HELD",
    "true, false, true, false, false, true, true, true, false, 5, 0, 3, true, true, false, true, false, false, false, false, false, false, false, false, false, HELD",
    "true, false, false, false, false, true, false, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, IN_WINGED_FORM",
    "true, false, false, false, false, true, true, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, IN_WINGED_FORM",
    "true, false, false, false, false, false, false, true, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, WEAKENED",
    "true, false, false, false, false, false, false, false, false, 1, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, COOLING",
    "true, false, false, false, false, false, false, false, false, 1, 20, 3, true, false, false, false, false, false, false, false, false, false, false, false, false, COOLING",
    "true, false, false, false, false, false, false, false, false, 0, 2, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, THIRSTY",
    "true, false, false, false, false, false, false, false, false, 0, 2, 3, true, false, false, false, false, false, false, false, false, false, false, false, false, THIRSTY",
    "true, false, false, false, false, false, true, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, AS_A_BAT",
    "true, false, false, false, false, false, true, true, false, 1, 0, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, AS_A_BAT",
    "true, false, false, false, false, false, true, false, false, 0, 20, 3, true, true, false, false, false, false, false, false, false, false, false, false, false, AS_A_BAT",
    "true, true, false, false, false, false, true, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, HELPLESS",
    "true, false, false, false, false, false, false, false, true, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, SUNLIT",
    "true, false, false, false, false, false, false, true, true, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, WEAKENED",
    "true, false, false, false, false, false, false, false, true, 5, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, SUNLIT",
    "true, false, false, false, false, false, false, false, true, 0, 20, 3, true, true, false, false, false, false, false, false, false, false, false, false, false, SUNLIT",
    "true, false, false, false, false, false, false, false, false, 0, 20, 3, true, false, false, false, false, false, false, false, false, false, false, false, false, UNSEEN",
    "true, false, false, false, false, false, false, false, false, 0, 20, 3, true, true, false, true, false, false, false, false, false, false, false, false, false, UNSEEN",
    "true, false, false, false, false, false, false, false, false, 0, 20, 3, false, true, false, false, false, false, false, false, false, false, false, false, false, NOWHERE",
    "true, false, false, false, false, false, false, false, false, 0, 2, 3, false, true, false, false, false, false, false, false, false, false, false, false, false, THIRSTY",
    "true, false, false, false, false, false, false, false, false, 0, 20, 3, false, false, false, true, false, false, false, false, false, false, false, false, false, NO_SUBJECT",
    "true, false, false, false, false, false, false, false, false, 0, 20, 3, false, true, false, true, false, false, false, false, false, false, false, false, false, NOWHERE",
    "true, false, false, true, false, false, false, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, BARRED",
    "true, false, true, true, true, false, false, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, HELD",
    "true, false, false, true, true, true, false, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, BARRED",
    "true, false, false, false, true, true, false, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, GROUNDED",
    "true, true, false, false, true, false, false, false, false, 0, 20, 3, false, false, false, false, false, false, false, false, false, false, false, false, false, HELPLESS",
    "true, false, false, false, false, false, false, false, false, 0, 20, 3, false, false, true, false, false, false, false, false, false, false, false, false, false, NO_GROUND",
    "true, false, false, false, false, false, false, false, false, 0, 20, 3, false, true, true, false, false, false, false, false, false, false, false, false, false, NOWHERE",
    "true, false, false, false, false, false, false, false, false, 0, 20, 3, false, false, true, true, false, false, false, false, false, false, false, false, false, NO_GROUND",
    "true, false, false, false, false, false, false, false, false, 0, 2, 3, false, false, true, false, false, false, false, false, false, false, false, false, false, THIRSTY",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, true, false, false, false, false, false, false, false, false, REBUKED",
    "true, false, true, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, true, false, false, false, false, false, false, false, false, HELD",
    "true, false, false, true, false, false, false, false, false, 0, 20, 0, false, false, false, false, true, false, false, false, false, false, false, false, false, REBUKED",
    "true, false, false, false, false, false, false, false, false, 5, 20, 0, false, false, false, false, true, false, false, false, false, false, false, false, false, REBUKED",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, true, false, false, false, false, false, false, false, NO_CRUCIFIX",
    "true, false, false, false, false, false, false, false, false, 5, 20, 0, false, false, false, false, false, true, false, false, false, false, false, false, false, COOLING",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, true, false, false, false, false, false, false, NO_WEAPON",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, true, false, false, false, true, false, false, false, false, false, false, NO_GROUND",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, true, false, false, false, false, false, TOO_HURT",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, true, true, false, false, false, false, false, NO_WEAPON",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, true, false, false, false, false, SMOTHERED",
    "true, false, false, false, false, false, false, false, false, 5, 20, 0, false, false, false, false, false, false, false, false, true, false, false, false, false, COOLING",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, true, false, true, false, false, false, false, NO_WEAPON",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, false, true, false, false, false, NO_STAKE",
    "true, false, false, false, false, false, false, false, false, 5, 20, 0, false, false, false, false, false, false, false, false, false, true, false, false, false, COOLING",
    "true, false, true, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, false, true, false, false, false, HELD",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, true, true, false, false, false, false, false, false, false, true, false, false, false, NO_STAKE",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, false, false, true, false, false, UNLOADED",
    "true, false, false, false, false, false, false, false, false, 5, 20, 0, false, false, false, false, false, false, false, false, false, false, true, false, false, COOLING",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, false, true, true, false, false, NO_STAKE",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, false, false, false, true, false, REVEALED",
    "true, false, true, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, false, false, false, true, false, HELD",
    "true, false, false, true, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, false, false, false, true, false, BARRED",
    "true, false, false, false, false, false, false, false, false, 5, 20, 0, false, false, false, false, false, false, false, false, false, false, false, true, false, REVEALED",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, false, false, false, false, false, false, false, false, false, false, false, false, true, NO_SCENE",
    "true, false, false, false, false, false, false, false, false, 5, 20, 0, false, false, false, false, false, false, false, false, false, false, false, false, true, COOLING",
    "true, false, false, false, false, false, false, false, false, 0, 20, 0, true, false, false, false, false, false, false, false, false, false, false, false, true, UNSEEN"
  })
  @DisplayName(
      "a power is refused while downed, held, barred, grounded, in winged form, in bat form, weakened, in the sun,"
          + " cooling, short of blood, with nothing to aim at, nowhere to step, no ground, no one to"
          + " order, rebuked, without a crucifix or a weapon, too hurt to bleed for it, or inside a"
          + " blood circle, or without a stake or a loaded crossbow, laid bare by a Revelation or with no scene to"
          + " read")
  void checks(
      final boolean hasPower,
      final boolean helpless,
      final boolean held,
      final boolean barred,
      final boolean grounded,
      final boolean winged,
      final boolean bat,
      final boolean weakened,
      final boolean sunlit,
      final long cooling,
      final int blood,
      final int cost,
      final boolean unseen,
      final boolean nowhere,
      final boolean groundless,
      final boolean subjectless,
      final boolean rebuked,
      final boolean crossless,
      final boolean unarmed,
      final boolean hurt,
      final boolean smothered,
      final boolean stakeless,
      final boolean unloaded,
      final boolean revealed,
      final boolean sceneless,
      final PowerUse.Verdict expected) {
    assertEquals(
        expected,
        PowerUse.check(
            new PowerUse.Situation(
                hasPower,
                helpless,
                held,
                barred,
                grounded,
                winged,
                bat,
                weakened,
                sunlit,
                cooling,
                blood,
                cost,
                unseen,
                nowhere,
                groundless,
                subjectless,
                rebuked,
                crossless,
                unarmed,
                hurt,
                smothered,
                stakeless,
                unloaded,
                revealed,
                sceneless)));
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
