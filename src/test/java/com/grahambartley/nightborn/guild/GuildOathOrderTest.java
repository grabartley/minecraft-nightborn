package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class GuildOathOrderTest {

  @ParameterizedTest(name = "beside {0}")
  @EnumSource(GuildCounter.Kind.class)
  @DisplayName("the oath is its own wheel action beside each counter, so it is gated alone")
  void ownAction(final GuildCounter.Kind kind) {
    assertEquals("nightborn:guild_oath", new GuildOathOrder().id().toString());
    assertNotEquals(new GuildCounterOrders(kind).id(), new GuildOathOrder().id());
  }
}
