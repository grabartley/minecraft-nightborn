package com.grahambartley.nightborn.sickness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BiteSicknessAttachmentTest {

  @Test
  @DisplayName("a healthy player stores nothing")
  void healthyStoresNothing() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    assertFalse(BiteSicknessAttachment.isSick(player));
    assertEquals(0, BiteSicknessAttachment.ticksLeft(player));
  }

  @ParameterizedTest(name = "{0} ticks")
  @ValueSource(ints = {1, 24000, 72000})
  void storesAndClears(final int ticks) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    BiteSicknessAttachment.set(player, ticks);
    assertTrue(BiteSicknessAttachment.isSick(player));
    assertEquals(ticks, BiteSicknessAttachment.ticksLeft(player));
    BiteSicknessAttachment.clear(player);
    assertFalse(BiteSicknessAttachment.isSick(player));
  }

  @Test
  @DisplayName("the sickness is saved and kept through death, and never synced to anyone")
  void followsTheGrid() {
    assertTrue(BiteSicknessAttachment.TICKS_LEFT.isPersistent());
    assertTrue(BiteSicknessAttachment.TICKS_LEFT.copyOnDeath());
    assertFalse(BiteSicknessAttachment.TICKS_LEFT.isSynced());
  }
}
