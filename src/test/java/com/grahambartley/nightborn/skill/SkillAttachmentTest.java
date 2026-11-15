package com.grahambartley.nightborn.skill;

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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SkillAttachmentTest {

  @Test
  @DisplayName("a player with no skills stores nothing")
  void emptyStoresNothing() {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    SkillAttachment.set(player, SkillTree.VAMPIRE, TreeProgress.EMPTY.withEarned(2));
    SkillAttachment.set(player, SkillTree.VAMPIRE, TreeProgress.EMPTY);
    assertFalse(player.hasAttached(SkillAttachment.TYPE));
    assertEquals(SkillProgress.EMPTY, SkillAttachment.get(player));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName("setting one tree leaves the other as it was")
  void setsOneTree(final SkillTree tree) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    SkillAttachment.set(
        player,
        SkillProgress.EMPTY
            .with(SkillTree.HUNTER, TreeProgress.EMPTY.withEarned(7))
            .with(SkillTree.VAMPIRE, TreeProgress.EMPTY.withEarned(3)));
    SkillAttachment.set(player, tree, TreeProgress.EMPTY.withEarned(11));
    assertEquals(11, SkillAttachment.get(player, tree).earned());
    assertEquals(
        tree == SkillTree.VAMPIRE ? 7 : 3,
        SkillAttachment.get(
                player, tree == SkillTree.VAMPIRE ? SkillTree.HUNTER : SkillTree.VAMPIRE)
            .earned());
  }

  static Stream<Arguments> rules() {
    return Stream.of(
        Arguments.of(
            "saved with the player", (Predicate<AttachmentType<?>>) AttachmentType::isPersistent),
        Arguments.of(
            "kept through death", (Predicate<AttachmentType<?>>) AttachmentType::copyOnDeath),
        Arguments.of(
            "synced to its player", (Predicate<AttachmentType<?>>) AttachmentType::isSynced));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rules")
  @DisplayName("skills follow the persistence grid")
  void followsTheGrid(final String rule, final Predicate<AttachmentType<?>> check) {
    assertEquals(true, check.test(SkillAttachment.TYPE), rule);
  }
}
