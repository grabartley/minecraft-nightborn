package com.grahambartley.nightborn.client.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

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
    assertThrows(
        NullPointerException.class, () -> new ClientState(null, FormViewLevel.FULL, false));
  }

  @Test
  @DisplayName("a missing form view level is refused")
  void nullFormViewsRefused() {
    assertThrows(NullPointerException.class, () -> new ClientState(GoreLevel.FULL, null, false));
  }

  @Test
  @DisplayName("form views are shown in full by default")
  void formViewsDefaultFull() {
    assertEquals(FormViewLevel.FULL, ClientState.DEFAULTS.formViews());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(FormViewLevel.class)
  @DisplayName("changing the form view level keeps it and leaves gore alone")
  void withFormViews(final FormViewLevel level) {
    final ClientState state = ClientState.DEFAULTS.withGore(GoreLevel.REDUCED).withFormViews(level);
    assertEquals(level, state.formViews());
    assertEquals(GoreLevel.REDUCED, state.gore());
  }

  @ParameterizedTest(name = "skip {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("changing the ceremony camera choice keeps the gore level")
  void withSkipCeremonyCamera(final boolean skip) {
    final ClientState state =
        ClientState.DEFAULTS.withGore(GoreLevel.OFF).withSkipCeremonyCamera(skip);
    assertEquals(skip, state.skipCeremonyCamera());
    assertEquals(GoreLevel.OFF, state.gore());
  }

  @Test
  @DisplayName("the ceremony camera is shown by default")
  void cameraShownByDefault() {
    assertEquals(false, ClientState.DEFAULTS.skipCeremonyCamera());
  }
}
