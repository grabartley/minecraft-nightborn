package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ScoutTicketTest {

  static final int LOADED_LEVEL = 33;
  static final int ENTITY_TICKING_LEVEL = 31;

  @ParameterizedTest(name = "{0} chunk(s) away")
  @ValueSource(ints = {0, 1})
  @DisplayName("the chunks a scout can step into within a refresh keep ticking entities")
  void keepsTheScoutMoving(final int chunksAway) {
    assertTrue(LOADED_LEVEL - ScoutTicket.RADIUS + chunksAway <= ENTITY_TICKING_LEVEL);
  }

  @ParameterizedTest(name = "refresh {0}")
  @ValueSource(ints = {ScoutTicket.REFRESH_TICKS})
  @DisplayName("a ticket lasts past the next refresh, and lapses soon after the scout stops")
  void outlastsARefresh(final int refresh) {
    assertTrue(ScoutTicket.EXPIRY_TICKS > refresh);
    assertTrue(ScoutTicket.EXPIRY_TICKS <= refresh * 10);
  }
}
