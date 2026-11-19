package com.grahambartley.nightborn.guild.raiding;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidingDataTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");

  @ParameterizedTest(name = "{0}")
  @CsvSource({"fledgling", "adult", "elder", "ancient"})
  void everyStageAPlayerCanReachHasARaidingParty(final String stage) throws IOException {
    assertTrue(
        JsonParser.parseString(
                Files.readString(DATA.resolve("vampire_stage").resolve(stage + ".json")))
            .getAsJsonObject()
            .has("age"));
    assertTrue(Files.exists(DATA.resolve("raiding_party").resolve(stage + ".json")));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"nightborn:raiding_party"})
  void registersItsDataFolder(final String registry) {
    assertTrue(RaidingData.ARMS.getValue().toString().equals(registry));
  }
}
