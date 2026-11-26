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
            "an unknown requirement on a skill further right",
            Map.of(
                id("core/a"), skill("core", 0, 0),
                id("core/b"), skill("core", 0, 1, "core/a", "core/ghost")),
            List.of("nightborn:core/b needs unknown skill nightborn:core/ghost")),
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
            "a skill further right than what it needs",
            Map.of(id("core/a"), skill("core", 0, 0), id("core/b"), skill("core", 0, 2, "core/a")),
            List.of(
                "nightborn:core/b is at row 2, but the deepest skill it needs puts it at row 1")),
        Arguments.of(
            "a skill level with what it needs",
            Map.of(id("core/a"), skill("core", 0, 0), id("core/b"), skill("core", 1, 0, "core/a")),
            List.of(
                "nightborn:core/b is at row 0, but the deepest skill it needs puts it at row 1")),
        Arguments.of(
            "a first skill away from the left edge",
            Map.of(id("core/a"), skill("core", 0, 1)),
            List.of(
                "nightborn:core/a is at row 1, but the deepest skill it needs puts it at row 0")),
        Arguments.of(
            "a loop of requirements",
            Map.of(
                id("core/a"), skill("core", 0, 0, "core/b"),
                id("core/b"), skill("core", 1, 1, "core/a")),
            List.of(
                "nightborn:core/a is at row 0, but the deepest skill it needs puts it at row 2",
                "nightborn:core/a needs itself through a loop of requirements",
                "nightborn:core/b needs itself through a loop of requirements")),
        Arguments.of(
            "two skills in one place",
            Map.of(id("core/a"), skill("core", 1, 0), id("core/b"), skill("core", 1, 0)),
            List.of(
                "nightborn:core/b shares its place in nightborn:core 1,0 with nightborn:core/a")),
        Arguments.of(
            "one skill fanning out to several",
            Map.of(
                id("core/a"), skill("core", 1, 0),
                id("core/b"), skill("core", 0, 1, "core/a"),
                id("core/c"), skill("core", 2, 1, "core/a"),
                id("core/d"), skill("core", 1, 2, "core/b", "core/c")),
            List.of()),
        Arguments.of(
            "a long line along an empty lane",
            Map.of(
                id("core/a"), skill("core", 0, 0),
                id("core/c"), skill("core", 1, 1, "core/a"),
                id("core/d"), skill("core", 1, 2, "core/c"),
                id("core/e"), skill("core", 0, 3, "core/a", "core/d")),
            List.of()),
        Arguments.of(
            "a skill sitting on a long line",
            Map.of(
                id("core/a"), skill("core", 0, 0),
                id("core/c"), skill("core", 1, 1, "core/a"),
                id("core/d"), skill("core", 1, 2, "core/c"),
                id("core/e"), skill("core", 0, 3, "core/a", "core/d"),
                id("core/x"), skill("core", 0, 1, "core/a")),
            List.of("nightborn:core/x sits on the line from nightborn:core/a to nightborn:core/e")),
        Arguments.of(
            "two unrelated lines crossing",
            Map.of(
                id("core/a"), skill("core", 0, 0),
                id("core/b"), skill("core", 1, 0),
                id("core/c"), skill("core", 1, 1, "core/a"),
                id("core/d"), skill("core", 0, 1, "core/b")),
            List.of(
                "the line from nightborn:core/a to nightborn:core/c touches the line from"
                    + " nightborn:core/b to nightborn:core/d")),
        Arguments.of(
            "a long line crossing another skill's line",
            Map.of(
                id("core/a"), skill("core", 2, 0),
                id("core/b"), skill("core", 4, 1, "core/a"),
                id("core/c"), skill("core", 4, 2, "core/b"),
                id("core/m"), skill("core", 1, 0),
                id("core/p"), skill("core", 1, 1, "core/m"),
                id("core/q"), skill("core", 3, 2, "core/p"),
                id("core/z"), skill("core", 2, 3, "core/a", "core/c")),
            List.of(
                "the line from nightborn:core/p to nightborn:core/q touches the line from"
                    + " nightborn:core/a to nightborn:core/z")),
        Arguments.of(
            "skills and lines in other branches never block or touch a line",
            Map.of(
                id("core/a"), skill("core", 0, 0),
                id("core/c"), skill("core", 1, 1, "core/a"),
                id("core/e"), skill("core", 0, 2, "core/a", "core/c"),
                id("tracking/m"), skill("tracking", 1, 0),
                id("tracking/x"), skill("tracking", 0, 1, "tracking/m")),
            List.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  @DisplayName("bad skill data is reported clearly when the server starts")
  void reports(
      final String name, final Map<Identifier, Skill> skills, final List<String> problems) {
    assertEquals(problems, SkillDataCheck.problems(new HashMap<>(skills), BRANCHES));
  }
}
