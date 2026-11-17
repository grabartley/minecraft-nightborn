package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeAttachmentTest {

  @Test
  @DisplayName(
      "a player with nothing saved is a fledgling, so a vampire from an older world is one")
  void absentIsFledgling() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    assertEquals(AgeProgress.EMPTY, AgeAttachment.get(player));
    AgeAttachment.set(player, AgeProgress.EMPTY);
    assertFalse(player.hasAttached(AgeAttachment.TYPE));
  }

  @ParameterizedTest(name = "rank {0}, {1} nights")
  @CsvSource({"0, 3", "1, 10", "3, 100"})
  @DisplayName("progress is kept, and setting it again does not write or re-sync")
  void keeps(final int rank, final int nights) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    final AgeProgress progress = new AgeProgress(rank, nights, 5, 0);
    AgeAttachment.set(player, progress);
    final int writes = player.writes();
    AgeAttachment.set(player, progress);
    assertEquals(progress, AgeAttachment.get(player));
    assertEquals(writes, player.writes());
  }

  @Test
  @DisplayName("clearing forgets the stage, the night being lived and the clock")
  void clears() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    AgeAttachment.set(player, new AgeProgress(2, 50, 600, 6));
    player.setAttached(AgeAttachment.WATCH, new NightWatch(4, 900, false));
    player.setAttached(AgeAttachment.CLOCK, 99L);
    AgeAttachment.clear(player);
    assertEquals(AgeProgress.EMPTY, AgeAttachment.get(player));
    assertFalse(player.hasAttached(AgeAttachment.WATCH));
    assertFalse(player.hasAttached(AgeAttachment.CLOCK));
  }

  @Test
  @DisplayName("age is saved, kept through death and synced only to its own player")
  void followsTheGrid() {
    assertTrue(AgeAttachment.TYPE.isPersistent());
    assertTrue(AgeAttachment.TYPE.copyOnDeath());
    assertTrue(AgeAttachment.TYPE.isSynced());
    assertTrue(AgeAttachment.WATCH.isPersistent());
    assertTrue(AgeAttachment.WATCH.copyOnDeath());
    assertFalse(AgeAttachment.WATCH.isSynced());
    assertFalse(AgeAttachment.CLOCK.isPersistent());
    assertFalse(AgeAttachment.CLOCK.isSynced());
  }
}
