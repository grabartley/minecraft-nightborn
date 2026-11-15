package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RiseRulesTest {

  @ParameterizedTest(name = "held {0}, burning {1}, until {2}, now {3}: {4}")
  @CsvSource({
    "true, false, , 100, BEGIN",
    "true, true, , 100, WAKE",
    "true, false, 130, 100, WAIT",
    "true, true, 130, 100, WAIT",
    "true, false, 130, 129, WAIT",
    "true, false, 130, 130, WAKE",
    "true, false, 130, 400, WAKE",
    "false, false, , 100, WAKE",
    "false, false, 130, 100, WAKE"
  })
  @DisplayName(
      "the first wake starts the rise unless the sleeper burns, later wakes and fire wait for it,"
          + " and it ends on time or when the sleeper can no longer stay")
  void wake(
      final boolean held,
      final boolean burning,
      final Long until,
      final long now,
      final RiseRules.Wake wake) {
    assertEquals(
        wake,
        RiseRules.wake(
            held, burning, until == null ? OptionalLong.empty() : OptionalLong.of(until), now));
  }

  @ParameterizedTest(name = "standing {0}: {1} ticks, clip {2}")
  @CsvSource({"false, 30, coffin_rise", "true, 20, coffin_step_out"})
  @DisplayName("the server holds the sleeper for exactly as long as its clip plays")
  void ticks(final boolean standing, final int ticks, final String clip) throws IOException {
    assertEquals(ticks, RiseRules.ticks(standing));
    final JsonObject animation =
        JsonParser.parseString(
                Files.readString(
                    Path.of(
                        "src/client/resources/assets/nightborn/animations/entity/vampire.animation.json")))
            .getAsJsonObject()
            .getAsJsonObject("animations")
            .getAsJsonObject(clip);
    assertEquals(ticks, animation.get("animation_length").getAsDouble() * 20.0, 1.0e-9, clip);
  }
}
