package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommandOrderTest {

  @ParameterizedTest(name = "door {0}, foe {1}: {2}")
  @CsvSource({
    "false, false, FOLLOW",
    "true, false, OPEN_DOOR",
    "false, true, ATTACK",
    "true, true, ATTACK"
  })
  @DisplayName("the order follows what the vampire looks at: a foe, then a door, else follow")
  void picks(final boolean door, final boolean foe, final CommandOrder order) {
    assertEquals(order, CommandOrder.pick(door, foe));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "FOLLOW, nightborn.command.follow",
    "ATTACK, nightborn.command.attack",
    "OPEN_DOOR, nightborn.command.open_door"
  })
  @DisplayName("each order has its own line")
  void keys(final CommandOrder order, final String key) {
    assertEquals(key, order.translationKey());
  }
}
