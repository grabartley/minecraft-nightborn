package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PowerCooldownAttachmentTest {

  @Test
  @DisplayName("no running cooldowns stores nothing")
  void emptyStoresNothing() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    final PowerCooldowns running =
        PowerCooldowns.NONE.start(Identifier.of("nightborn", "core/blood_rush"), 0, 600);
    PowerCooldownAttachment.set(player, running);
    assertTrue(player.hasAttached(PowerCooldownAttachment.TYPE));
    assertEquals(running, PowerCooldownAttachment.get(player));
    PowerCooldownAttachment.set(player, PowerCooldowns.NONE);
    assertFalse(player.hasAttached(PowerCooldownAttachment.TYPE));
  }

  static Stream<Arguments> rules() {
    return Stream.of(
        Arguments.of(
            "saved, so leaving does not reset a cooldown",
            (Predicate<AttachmentType<?>>) AttachmentType::isPersistent),
        Arguments.of(
            "kept through death", (Predicate<AttachmentType<?>>) AttachmentType::copyOnDeath),
        Arguments.of(
            "synced to its player for the bar",
            (Predicate<AttachmentType<?>>) AttachmentType::isSynced));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rules")
  @DisplayName("cooldowns are per-player state")
  void rules(final String rule, final Predicate<AttachmentType<?>> check) {
    assertTrue(check.test(PowerCooldownAttachment.TYPE), rule);
  }
}
