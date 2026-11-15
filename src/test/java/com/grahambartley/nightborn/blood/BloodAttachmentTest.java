package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class BloodAttachmentTest {

  @Test
  @DisplayName("nothing saved means a full blood bar, so a human or fresh vampire stores nothing")
  void absentIsFull() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    assertEquals(BloodAttachment.FULL, BloodAttachment.get(player));
    BloodAttachment.set(player, BloodAttachment.FULL);
    assertFalse(player.hasAttached(BloodAttachment.TYPE));
  }

  @ParameterizedTest(name = "set {0} -> {1}")
  @CsvSource({"-5, 0", "0, 0", "7, 7", "19, 19", "20, 20", "35, 20"})
  @DisplayName("blood is kept between empty and full")
  void clamps(final int set, final int read) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    BloodAttachment.set(player, set);
    assertEquals(read, BloodAttachment.get(player));
  }

  @Test
  @DisplayName("setting the same value again does not write, so nothing re-syncs")
  void unchangedDoesNotWrite() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    BloodAttachment.set(player, 12);
    final int writes = player.writes();
    BloodAttachment.set(player, 12);
    assertEquals(writes, player.writes());
  }

  static Stream<Arguments> rules() {
    return Stream.of(
        Arguments.of(
            "saved with the player",
            (Predicate<AttachmentType<?>>) AttachmentType::isPersistent,
            true),
        Arguments.of(
            "refilled on death", (Predicate<AttachmentType<?>>) AttachmentType::copyOnDeath, false),
        Arguments.of(
            "synced to its player", (Predicate<AttachmentType<?>>) AttachmentType::isSynced, true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rules")
  @DisplayName("blood follows the persistence grid")
  void followsTheGrid(
      final String rule, final Predicate<AttachmentType<?>> check, final boolean holds) {
    assertEquals(holds, check.test(BloodAttachment.TYPE), rule);
  }
}
