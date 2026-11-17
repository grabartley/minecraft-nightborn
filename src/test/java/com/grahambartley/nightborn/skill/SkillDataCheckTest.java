package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SkillDataCheckTest {

  static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  static Skill skill(final String branch, final int column, final int row, final String... needs) {
    return new Skill(
        id(branch),
        Stream.of(needs).map(SkillDataCheckTest::id).toList(),
        id("icon"),
        column,
        row,
        Optional.empty(),
        List.of(SkillRank.costing(1)),
        Optional.empty());
  }

  static final Map<Identifier, SkillBranch> BRANCHES =
      Map.of(
          id("core"), new SkillBranch(SkillTree.VAMPIRE, false, 0),
          id("tracking"), new SkillBranch(SkillTree.HUNTER, false, 0));

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of(
            "a clean tree",
            Map.of(id("core/a"), skill("core", 0, 0), id("core/b"), skill("core", 1, 1, "core/a")),
            List.of()),
        Arguments.of(
            "an unknown branch",
            Map.of(id("x/a"), skill("x", 0, 0)),
            List.of("nightborn:x/a is in unknown branch nightborn:x")),
        Arguments.of(
            "an unknown requirement",
            Map.of(id("core/a"), skill("core", 0, 0, "core/ghost")),
            List.of("nightborn:core/a needs unknown skill nightborn:core/ghost")),
        Arguments.of(
            "a skill needing itself",
            Map.of(id("core/a"), skill("core", 0, 0, "core/a")),
            List.of("nightborn:core/a needs itself")),
        Arguments.of(
            "a requirement from the other tree",
            Map.of(
                id("core/a"),
                skill("core", 0, 0, "tracking/a"),
                id("tracking/a"),
                skill("tracking", 0, 0)),
            List.of("nightborn:core/a needs nightborn:tracking/a from another tree")),
        Arguments.of(
            "a requirement on the same row",
            Map.of(id("core/a"), skill("core", 0, 1), id("core/b"), skill("core", 1, 1, "core/a")),
            List.of("nightborn:core/b needs nightborn:core/a, which is not on a row above it")),
        Arguments.of(
            "a loop of requirements",
            Map.of(
                id("core/a"), skill("core", 0, 0, "core/b"),
                id("core/b"), skill("core", 1, 1, "core/a")),
            List.of(
                "nightborn:core/a needs nightborn:core/b, which is not on a row above it",
                "nightborn:core/a needs itself through a loop of requirements",
                "nightborn:core/b needs itself through a loop of requirements")),
        Arguments.of(
            "two skills in one place",
            Map.of(id("core/a"), skill("core", 1, 2), id("core/b"), skill("core", 1, 2)),
            List.of(
                "nightborn:core/b shares its place in nightborn:core 1,2 with nightborn:core/a")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  @DisplayName("bad skill data is reported clearly when the server starts")
  void reports(
      final String name, final Map<Identifier, Skill> skills, final List<String> problems) {
    assertEquals(problems, SkillDataCheck.problems(new HashMap<>(skills), BRANCHES));
  }
}
