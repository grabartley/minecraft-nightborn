package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.SkillTree;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChallengeRewardsTest {

  @ParameterizedTest(name = "a {0} trial pays into {1}")
  @CsvSource({"VAMPIRE, VAMPIRE", "HUNTER, HUNTER", "ANYONE, "})
  void paysIntoTheTreeOfItsSide(final Side side, final SkillTree tree) {
    assertEquals(Optional.ofNullable(tree), ChallengeRewards.tree(side));
  }
}
