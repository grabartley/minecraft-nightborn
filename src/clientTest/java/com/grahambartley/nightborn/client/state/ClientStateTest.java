package com.grahambartley.nightborn.client.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ClientStateTest {

  @Test
  @DisplayName("gore is shown in full by default")
  void defaultIsFull() {
    assertEquals(GoreLevel.FULL, ClientState.DEFAULTS.gore());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(GoreLevel.class)
  @DisplayName("changing the gore level keeps the new level")
  void withGore(final GoreLevel level) {
    assertEquals(level, ClientState.DEFAULTS.withGore(level).gore());
  }

  @Test
  @DisplayName("a missing gore level is refused")
  void nullRefused() {
    assertThrows(NullPointerException.class, () -> new ClientState(null));
  }
}
