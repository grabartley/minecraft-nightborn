package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @ParameterizedTest(name = "held {0}, until {1}, now {2}: {3}")
  @CsvSource({
    "true, , 100, BEGIN",
    "true, 130, 100, WAIT",
    "true, 130, 129, WAIT",
    "true, 130, 130, WAKE",
    "true, 130, 400, WAKE",
    "false, , 100, WAKE",
    "false, 130, 100, WAKE"
  })
  @DisplayName(
      "the first wake starts the rise, later wakes wait for it, and it ends on time or when"
          + " the sleeper can no longer stay")
  void wake(final boolean held, final Long until, final long now, final RiseRules.Wake wake) {
    assertEquals(
        wake,
        RiseRules.wake(held, until == null ? OptionalLong.empty() : OptionalLong.of(until), now));
  }

  @ParameterizedTest(name = "standing {0}: {1} ticks, clip {2}")
  @CsvSource({"false, 30, coffin_rise", "true, 20, coffin_step_out"})
  @DisplayName("the server holds the sleeper for at least as long as its clip plays")
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
    assertTrue(
        animation.get("animation_length").getAsDouble() * 20.0 <= ticks,
        clip + " ends within the hold");
  }
}
