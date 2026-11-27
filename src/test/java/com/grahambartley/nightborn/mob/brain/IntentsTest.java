package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IntentsTest {

  @ParameterizedTest(name = "goals [{0}], target {1}: {2}")
  @CsvSource({
    "'', true, FIGHT",
    "'', false, REST",
    "CLOSE_IN, true, CLOSE_IN",
    "CLOSE_IN, false, REST",
    "RETREAT, true, RETREAT",
    "CLOSE_IN RETREAT, true, RETREAT",
    "TRAVEL, true, FIGHT",
    "TRAVEL, false, TRAVEL",
    "REST, false, REST",
    "GUARD TRAVEL, false, GUARD"
  })
  @DisplayName(
      "falling back comes first, then the fight with a target, then what the running goals say")
  void pick(final String goals, final boolean target, final Intent intent) {
    final List<Intent> reported =
        goals.isBlank() ? List.of() : Arrays.stream(goals.split(" ")).map(Intent::valueOf).toList();
    assertEquals(intent, Intents.pick(reported, target));
  }
}
