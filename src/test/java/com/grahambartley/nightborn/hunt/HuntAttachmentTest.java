package com.grahambartley.nightborn.hunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HuntAttachmentTest {

  @Test
  @DisplayName("notoriety is saved and kept through death but never sent to any client")
  void neverSynced() {
    assertTrue(HuntAttachment.RECORD.isPersistent());
    assertTrue(HuntAttachment.RECORD.copyOnDeath());
    assertFalse(HuntAttachment.RECORD.isSynced());
    assertFalse(HuntAttachment.CLOCK.isSynced());
    assertFalse(HuntAttachment.CALL.isSynced());
  }

  @Test
  @DisplayName("a bell's build-up is not saved, so leaving the server ends it")
  void callNotSaved() {
    assertFalse(HuntAttachment.CALL.isPersistent());
    assertFalse(HuntAttachment.CLOCK.isPersistent());
  }

  @Test
  @DisplayName("the clock carries over a respawn, so the dusk or dawn on that tick still counts")
  void clockSurvivesRespawn() {
    assertTrue(HuntAttachment.CLOCK.copyOnDeath());
    assertFalse(HuntAttachment.CALL.copyOnDeath());
  }

  @ParameterizedTest(name = "notoriety {0}")
  @CsvSource({"0.5", "31", "200"})
  @DisplayName("clearing forgets notoriety, the hunt, the clock and any ringing bell")
  void clears(final double notoriety) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    player.setAttached(HuntAttachment.RECORD, HuntRecord.EMPTY.withNotoriety(notoriety).warning(2));
    player.setAttached(HuntAttachment.CLOCK, 99L);
    player.setAttached(HuntAttachment.CALL, BellCall.rung(200));
    HuntAttachment.clear(player);
    assertEquals(HuntRecord.EMPTY, HuntAttachment.get(player));
    assertFalse(player.hasAttached(HuntAttachment.CLOCK));
    assertFalse(player.hasAttached(HuntAttachment.CALL));
  }
}
