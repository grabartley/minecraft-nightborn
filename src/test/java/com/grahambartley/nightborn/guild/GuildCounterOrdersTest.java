package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildCounterOrdersTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"GEAR, nightborn:guild_gear", "CONTRACTS, nightborn:guild_contracts"})
  @DisplayName("each counter is its own wheel action, so later ranks can gate one alone")
  void id(final GuildCounter.Kind kind, final String id) {
    assertEquals(id, new GuildCounterOrders(kind).id().toString());
  }
}
