package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodCapacityTest {

  @ParameterizedTest(name = "{0} extra -> a bar of {1}")
  @CsvSource({"0, 20", "2, 22", "6, 26", "40, 60", "99, 60", "-4, 20"})
  @DisplayName("the bar is twenty points plus what age adds, within a cap")
  void sizes(final int extra, final int capacity) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    BloodCapacity.setExtra(player, extra);
    assertEquals(capacity, BloodCapacity.of(player));
  }

  @Test
  @DisplayName("a mob or a fledgling stores nothing and has the plain bar")
  void absentIsPlain() {
    final FakeAttachmentTarget mob = new FakeAttachmentTarget();
    assertEquals(BloodAttachment.FULL, BloodCapacity.of(mob));
    BloodCapacity.setExtra(mob, 0);
    assertFalse(mob.hasAttached(BloodCapacity.EXTRA));
  }

  @Test
  @DisplayName("setting the same size again does not write, so nothing re-syncs")
  void unchangedDoesNotWrite() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    BloodCapacity.setExtra(player, 4);
    final int writes = player.writes();
    BloodCapacity.setExtra(player, 4);
    assertEquals(writes, player.writes());
  }

  @Test
  @DisplayName("the bar is saved, kept through death and synced only to its own player")
  void followsTheGrid() {
    assertTrue(BloodCapacity.EXTRA.isPersistent());
    assertTrue(BloodCapacity.EXTRA.copyOnDeath());
    assertTrue(BloodCapacity.EXTRA.isSynced());
  }
}
