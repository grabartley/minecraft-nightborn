package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class StakePenaltyTest {

  private static final Identifier NOBLE = Identifier.of("nightborn", "noble");
  private static final Identifier BEAST = Identifier.of("nightborn", "beast");
  private static final Identifier CORE_A = Identifier.of("nightborn", "core_a");
  private static final Identifier CORE_B = Identifier.of("nightborn", "core_b");
  private static final Identifier NOBLE_A = Identifier.of("nightborn", "noble_a");
  private static final Identifier NOBLE_B = Identifier.of("nightborn", "noble_b");
  private static final Identifier BEAST_A = Identifier.of("nightborn", "beast_a");

  private static final Function<Identifier, Optional<Identifier>> LINES =
      skill ->
          Optional.ofNullable(Map.of(NOBLE_A, NOBLE, NOBLE_B, NOBLE, BEAST_A, BEAST).get(skill));

  @ParameterizedTest(name = "{0} earned at {1}%: loses {2}")
  @CsvSource({"100, 10, 10", "15, 10, 2", "5, 10, 1", "0, 10, 0", "40, 0, 0", "40, 100, 40"})
  @DisplayName("the loss is the set share of earned points, rounded up")
  void pointsLost(final int earned, final int percent, final int lost) {
    assertEquals(lost, StakePenalty.pointsLost(earned, percent));
  }

  static Stream<Arguments> cases() {
    final List<BoughtSkill> three =
        List.of(
            new BoughtSkill(CORE_A, 3), new BoughtSkill(NOBLE_A, 4), new BoughtSkill(CORE_B, 2));
    final TreeProgress spentSix = new TreeProgress(20, 0.5F, three.subList(0, 2), List.of(NOBLE));
    final TreeProgress spentNine = new TreeProgress(9, 0.0F, three, List.of(NOBLE));
    final TreeProgress twoLines =
        new TreeProgress(
            9,
            0.0F,
            List.of(new BoughtSkill(NOBLE_A, 4), new BoughtSkill(BEAST_A, 5)),
            List.of(NOBLE, BEAST));
    return Stream.of(
        Arguments.of(
            "enough unspent points cover the loss",
            spentSix,
            new TreeProgress(18, 0.5F, three.subList(0, 2), List.of(NOBLE))),
        Arguments.of(
            "the most recent skill goes when unspent points run short",
            spentNine,
            new TreeProgress(8, 0.0F, three.subList(0, 2), List.of(NOBLE))),
        Arguments.of(
            "a bloodline with no skill left is dropped",
            twoLines,
            new TreeProgress(8, 0.0F, List.of(new BoughtSkill(NOBLE_A, 4)), List.of(NOBLE))),
        Arguments.of(
            "a bloodline whose skills are unknown is kept when none were removed",
            new TreeProgress(
                20,
                0.0F,
                List.of(new BoughtSkill(Identifier.of("nightborn", "gone"), 4)),
                List.of(BEAST)),
            new TreeProgress(
                18,
                0.0F,
                List.of(new BoughtSkill(Identifier.of("nightborn", "gone"), 4)),
                List.of(BEAST))),
        Arguments.of(
            "a bloodline keeps a skill that was not removed",
            new TreeProgress(
                5,
                0.0F,
                List.of(new BoughtSkill(NOBLE_A, 3), new BoughtSkill(NOBLE_B, 2)),
                List.of(NOBLE)),
            new TreeProgress(4, 0.0F, List.of(new BoughtSkill(NOBLE_A, 3)), List.of(NOBLE))),
        Arguments.of("nothing earned loses nothing", TreeProgress.EMPTY, TreeProgress.EMPTY));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  @DisplayName("a staked vampire loses unspent points first, then its most recent skills")
  void apply(final String name, final TreeProgress before, final TreeProgress after) {
    assertEquals(after, StakePenalty.apply(before, 10, LINES));
  }
}
