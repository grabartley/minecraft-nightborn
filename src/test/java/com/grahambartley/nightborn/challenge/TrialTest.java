package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TrialTest {

  static final Path LANG = Path.of("src/main/resources/assets/nightborn/lang/en_us.json");
  static final Set<String> GATES =
      Set.of("nightborn:adult", "nightborn:elder", "nightborn:hunter", "nightborn:veteran");

  @ParameterizedTest(name = "{0} has 20 trials")
  @CsvSource({"VAMPIRE", "HUNTER"})
  void eachSideHasTwentyTrials(final Side side) throws IOException {
    assertEquals(
        20,
        ChallengeDataFiles.load("trial", Trial.CODEC).values().stream()
            .filter(trial -> trial.side() == side)
            .count());
  }

  @Test
  void everyShippedTrialHasEnglishTextAKnownGateAndAShareOfAPoint() throws IOException {
    final JsonObject english = JsonParser.parseString(Files.readString(LANG)).getAsJsonObject();
    final Map<String, Trial> trials = ChallengeDataFiles.load("trial", Trial.CODEC);
    assertEquals(40, trials.size());
    trials.forEach(
        (id, trial) -> {
          assertTrue(english.has(key(trial.name())), id + " name");
          assertTrue(english.has(key(trial.description())), id + " description");
          trial.gate().ifPresent(gate -> assertTrue(GATES.contains(gate.toString()), id + " gate"));
          assertTrue(trial.size().share() > 0.0F, id);
        });
  }

  private static String key(final net.minecraft.text.Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }
}
