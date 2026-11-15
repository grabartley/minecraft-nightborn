package com.grahambartley.nightborn.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VisibleStateAttachmentTest {

  @Test
  @DisplayName("nothing to see stores nothing, so a human costs nothing")
  void noneStoresNothing() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    VisibleStateAttachment.set(player, new VisibleState(true, false, 0));
    VisibleStateAttachment.set(player, VisibleState.NONE);
    assertFalse(player.hasAttached(VisibleStateAttachment.TYPE));
    assertEquals(VisibleState.NONE, VisibleStateAttachment.get(player));
  }

  @Test
  @DisplayName("an unchanged state is not written again, so it is not re-sent every tick")
  void unchangedNotWritten() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    VisibleStateAttachment.set(player, new VisibleState(false, true, 100));
    final int writes = player.writes();
    VisibleStateAttachment.set(player, new VisibleState(false, true, 100));
    assertEquals(writes, player.writes());
  }

  @Test
  @DisplayName("it is synced to every tracking player and never saved")
  void syncedNotSaved() {
    assertTrue(VisibleStateAttachment.TYPE.isSynced());
    assertFalse(VisibleStateAttachment.TYPE.isPersistent());
  }
}
