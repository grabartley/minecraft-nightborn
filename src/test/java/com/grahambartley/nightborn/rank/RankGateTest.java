package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillRank;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankGateTest {

  @ParameterizedTest(name = "[{0}] -> {1}")
  @CsvSource({"'', -1", "initiate, 0", "veteran, 2", "master, 3", "lost, 2147483647"})
  @DisplayName(
      "a skill needs the place of the rank it names, nothing when it names none, and is shut when"
          + " the rank is unknown")
  void needed(final String rank, final int needed) {
    final Skill skill =
        new Skill(
            Identifier.of("nightborn", "slayer"),
            List.of(),
            Identifier.of("nightborn", "icon"),
            0,
            0,
            Optional.empty(),
            rank.isEmpty() ? Optional.empty() : Optional.of(Identifier.of("nightborn", rank)),
            List.of(SkillRank.costing(1)),
            Optional.empty());
    assertEquals(
        needed < 0 ? OptionalInt.empty() : OptionalInt.of(needed),
        RankGate.needed(RankFixtures.LADDER, skill));
  }
}
