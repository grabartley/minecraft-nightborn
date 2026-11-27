package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class IntentTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Intent.class)
  @DisplayName("each intent is found by its data name")
  void byKey(final Intent intent) {
    assertEquals(Optional.of(intent), Intent.byKey(intent.key()));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"close_in, CLOSE_IN", "fight, FIGHT", "FIGHT, ", "flee, "})
  @DisplayName("an intent's data name is its lower case name, and nothing else matches")
  void names(final String key, final Intent intent) {
    assertEquals(Optional.ofNullable(intent), Intent.byKey(key));
  }
}
