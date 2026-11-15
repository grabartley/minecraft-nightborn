package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StarvingAttachmentTest {

  @ParameterizedTest(name = "set {0} -> {1}, stored {2}")
  @CsvSource({"-3, 0, false", "0, 0, false", "1, 1, true", "12000, 12000, true"})
  @DisplayName("only a vampire that is starving stores anything")
  void stores(final int set, final int read, final boolean stored) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    StarvingAttachment.set(player, set);
    assertEquals(read, StarvingAttachment.get(player));
    assertEquals(stored, player.hasAttached(StarvingAttachment.TYPE));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"7"})
  @DisplayName("ending the starving clears what was saved")
  void clears(final int ticks) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    StarvingAttachment.set(player, ticks);
    StarvingAttachment.set(player, 0);
    assertFalse(player.hasAttached(StarvingAttachment.TYPE));
  }
}
