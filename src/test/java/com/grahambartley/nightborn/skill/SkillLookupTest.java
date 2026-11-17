package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillLookupTest {

  static final Skill SKILL =
      new Skill(
          Identifier.of("nightborn", "core"),
          List.of(),
          Identifier.of("nightborn", "icon"),
          0,
          0,
          Optional.empty(),
          List.of(SkillRank.costing(1)),
          Optional.empty());

  @ParameterizedTest(name = "needs {0}, at rank {1}: reached {2}")
  @CsvSource({"-1, 0, true", "1, 0, false", "1, 1, true", "2, 3, true", "3, 2, false"})
  @DisplayName("a skill's stage is reached when the buyer's rank is at least the one it needs")
  void stageReached(final int needs, final int rank, final boolean reached) {
    final OptionalInt needed = needs < 0 ? OptionalInt.empty() : OptionalInt.of(needs);
    assertEquals(reached, SkillLookup.stageReached(SKILL, skill -> needed, rank));
  }

  @ParameterizedTest(name = "rank {0}")
  @CsvSource({"0", "5"})
  @DisplayName("an unknown skill is left to the purchase check to refuse")
  void unknownSkill(final int rank) {
    assertEquals(true, SkillLookup.stageReached(null, skill -> OptionalInt.of(99), rank));
  }
}
