package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SkillPurchaseTest {

  static final Identifier CORE = Identifier.of("nightborn", "core");
  static final Identifier NOBLE = Identifier.of("nightborn", "noble");
  static final Identifier BEAST = Identifier.of("nightborn", "beast");
  static final Identifier OCCULT = Identifier.of("nightborn", "occult");
  static final Identifier ROOT = Identifier.of("nightborn", "core/root");
  static final Identifier CHILD = Identifier.of("nightborn", "core/child");
  static final Identifier OCCULT_ENTRY = Identifier.of("nightborn", "occult/entry");

  static final SkillBranch CORE_BRANCH = new SkillBranch(SkillTree.VAMPIRE, false, 0);
  static final SkillBranch BLOODLINE = new SkillBranch(SkillTree.VAMPIRE, true, 1);
  static final SkillBranch HUNTER = new SkillBranch(SkillTree.HUNTER, false, 0);

  static Skill skill(final Identifier branch, final int cost, final Identifier... requires) {
    return new Skill(branch, cost, List.of(requires), ROOT, 0, 0, List.of(), 0.0, Optional.empty());
  }

  static TreeProgress twoBloodlines(final int earned) {
    return TreeProgress.EMPTY
        .withEarned(earned)
        .buy(Identifier.of("nightborn", "noble/entry"), 1, NOBLE)
        .buy(Identifier.of("nightborn", "beast/entry"), 1, BEAST);
  }

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of(
            "affordable root",
            TreeProgress.EMPTY.withEarned(1),
            SkillTree.VAMPIRE,
            ROOT,
            skill(CORE, 1),
            CORE_BRANCH,
            SkillPurchase.Verdict.ALLOWED),
        Arguments.of(
            "unknown skill",
            TreeProgress.EMPTY.withEarned(9),
            SkillTree.VAMPIRE,
            ROOT,
            null,
            CORE_BRANCH,
            SkillPurchase.Verdict.UNKNOWN),
        Arguments.of(
            "unknown branch",
            TreeProgress.EMPTY.withEarned(9),
            SkillTree.VAMPIRE,
            ROOT,
            skill(CORE, 1),
            null,
            SkillPurchase.Verdict.UNKNOWN),
        Arguments.of(
            "skill only vampire mobs can hold",
            TreeProgress.EMPTY.withEarned(9),
            SkillTree.VAMPIRE,
            ROOT,
            skill(CORE, 1),
            new SkillBranch(SkillTree.VAMPIRE, false, 15, false),
            SkillPurchase.Verdict.UNKNOWN),
        Arguments.of(
            "human buying a vampire skill",
            TreeProgress.EMPTY.withEarned(9),
            SkillTree.HUNTER,
            ROOT,
            skill(CORE, 1),
            CORE_BRANCH,
            SkillPurchase.Verdict.WRONG_TREE),
        Arguments.of(
            "vampire buying a hunter skill",
            TreeProgress.EMPTY.withEarned(9),
            SkillTree.VAMPIRE,
            ROOT,
            skill(CORE, 1),
            HUNTER,
            SkillPurchase.Verdict.WRONG_TREE),
        Arguments.of(
            "already owned",
            TreeProgress.EMPTY.withEarned(9).buy(ROOT, 1, null),
            SkillTree.VAMPIRE,
            ROOT,
            skill(CORE, 1),
            CORE_BRANCH,
            SkillPurchase.Verdict.OWNED),
        Arguments.of(
            "third bloodline",
            twoBloodlines(9),
            SkillTree.VAMPIRE,
            OCCULT_ENTRY,
            skill(OCCULT, 1),
            BLOODLINE,
            SkillPurchase.Verdict.BLOODLINE_LOCKED),
        Arguments.of(
            "third bloodline with no points",
            twoBloodlines(2),
            SkillTree.VAMPIRE,
            OCCULT_ENTRY,
            skill(OCCULT, 1),
            BLOODLINE,
            SkillPurchase.Verdict.BLOODLINE_LOCKED),
        Arguments.of(
            "second skill in a chosen bloodline",
            twoBloodlines(9),
            SkillTree.VAMPIRE,
            Identifier.of("nightborn", "noble/more"),
            skill(NOBLE, 1),
            BLOODLINE,
            SkillPurchase.Verdict.ALLOWED),
        Arguments.of(
            "core skill after two bloodlines",
            twoBloodlines(9),
            SkillTree.VAMPIRE,
            ROOT,
            skill(CORE, 1),
            CORE_BRANCH,
            SkillPurchase.Verdict.ALLOWED),
        Arguments.of(
            "missing requirement",
            TreeProgress.EMPTY.withEarned(9),
            SkillTree.VAMPIRE,
            CHILD,
            skill(CORE, 1, ROOT),
            CORE_BRANCH,
            SkillPurchase.Verdict.NEEDS_SKILL),
        Arguments.of(
            "requirement owned",
            TreeProgress.EMPTY.withEarned(9).buy(ROOT, 1, null),
            SkillTree.VAMPIRE,
            CHILD,
            skill(CORE, 2, ROOT),
            CORE_BRANCH,
            SkillPurchase.Verdict.ALLOWED),
        Arguments.of(
            "one point short",
            TreeProgress.EMPTY.withEarned(1),
            SkillTree.VAMPIRE,
            ROOT,
            skill(CORE, 2),
            CORE_BRANCH,
            SkillPurchase.Verdict.TOO_FEW_POINTS),
        Arguments.of(
            "points already spent",
            TreeProgress.EMPTY.withEarned(2).buy(CHILD, 2, null),
            SkillTree.VAMPIRE,
            ROOT,
            skill(CORE, 1),
            CORE_BRANCH,
            SkillPurchase.Verdict.TOO_FEW_POINTS));
  }

  @ParameterizedTest(name = "{0} -> {6}")
  @MethodSource("cases")
  @DisplayName("the server checks tree, ownership, bloodline limit, order and points")
  void checks(
      final String name,
      final TreeProgress progress,
      final SkillTree tree,
      final Identifier id,
      final Skill skill,
      final SkillBranch branch,
      final SkillPurchase.Verdict expected) {
    assertEquals(expected, SkillPurchase.check(progress, tree, id, skill, branch));
  }

  @Test
  @DisplayName("buying a first bloodline skill chooses it, and a core skill chooses nothing")
  void choosesBloodlines() {
    final TreeProgress afterCore =
        SkillPurchase.apply(TreeProgress.EMPTY.withEarned(3), ROOT, skill(CORE, 1), CORE_BRANCH);
    assertEquals(List.of(), afterCore.bloodlines());
    final TreeProgress afterOccult =
        SkillPurchase.apply(afterCore, OCCULT_ENTRY, skill(OCCULT, 1), BLOODLINE);
    assertEquals(List.of(OCCULT), afterOccult.bloodlines());
    assertEquals(1, afterOccult.free());
  }

  static Stream<Arguments> bloodlines() {
    return Stream.of(
        Arguments.of("a bloodline skill", skill(NOBLE, 1), BLOODLINE, NOBLE),
        Arguments.of("a core skill", skill(CORE, 1), CORE_BRANCH, null),
        Arguments.of("an unknown skill", null, BLOODLINE, null),
        Arguments.of("an unknown branch", skill(NOBLE, 1), null, null));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("bloodlines")
  @DisplayName("only a skill in a bloodline branch belongs to a bloodline")
  void bloodline(
      final String name, final Skill skill, final SkillBranch branch, final Identifier line) {
    assertEquals(line, SkillPurchase.bloodline(skill, branch));
  }
}
