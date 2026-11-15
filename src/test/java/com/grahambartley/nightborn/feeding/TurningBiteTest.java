package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TurningBiteTest {

  @Test
  @DisplayName("a vampire's bite feeds until they choose otherwise, and stores nothing")
  void feedsByDefault() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    assertFalse(TurningBite.isOn(player));
    assertFalse(player.hasAttached(TurningBite.TYPE));
  }

  @ParameterizedTest(name = "set {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("the choice is stored only while the turning bite is on")
  void storesOnlyWhenOn(final boolean on) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    TurningBite.set(player, true);
    TurningBite.set(player, on);
    assertEquals(on, TurningBite.isOn(player));
    assertEquals(on, player.hasAttached(TurningBite.TYPE));
  }
}
