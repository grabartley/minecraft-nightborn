package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class RiteCooldownAttachmentTest {

  @ParameterizedTest(name = "waiting until {0} is read as {1}, stored {2}")
  @CsvSource({"24000, 24000, true", "1, 1, true", "0, 0, false", "-5, 0, false"})
  @DisplayName("a wait is kept until it is cleared, and no wait stores nothing")
  void waits(final long until, final long read, final boolean stored) {
    final FakeAttachmentTarget hunter = new FakeAttachmentTarget();
    assertEquals(RiteCooldownAttachment.READY, RiteCooldownAttachment.readyAt(hunter));
    RiteCooldownAttachment.wait(hunter, 500L);
    RiteCooldownAttachment.wait(hunter, until);
    assertEquals(read, RiteCooldownAttachment.readyAt(hunter));
    assertEquals(stored, hunter.hasAttached(RiteCooldownAttachment.TYPE));
  }

  static Stream<Arguments> rules() {
    return Stream.of(
        Arguments.of(
            "saved, so leaving does not reset the wait",
            (Predicate<AttachmentType<?>>) AttachmentType::isPersistent),
        Arguments.of(
            "kept through death", (Predicate<AttachmentType<?>>) AttachmentType::copyOnDeath));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rules")
  @DisplayName("the wait between rites is per-player state")
  void rules(final String rule, final Predicate<AttachmentType<?>> check) {
    assertTrue(check.test(RiteCooldownAttachment.TYPE), rule);
  }
}
