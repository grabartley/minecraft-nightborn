package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillRank;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StageGateTest {

  static final Identifier ADULT = Identifier.of("nightborn", "adult");
  static final Identifier ELDER = Identifier.of("nightborn", "elder");
  static final Map<Identifier, VampireStage> STAGES =
      Map.of(ADULT, StageFixtures.stage(1, 10), ELDER, StageFixtures.stage(2, 10));

  static Skill gated(final String stage) {
    return new Skill(
        Identifier.of("nightborn", "core"),
        List.of(),
        Identifier.of("nightborn", "icon"),
        0,
        0,
        stage.isEmpty() ? Optional.empty() : Optional.of(Identifier.of("nightborn", stage)),
        List.of(SkillRank.costing(1)),
        Optional.empty());
  }

  @ParameterizedTest(name = "needs {0}, at rank {1}: open {2}")
  @CsvSource({
    "'', 0, true",
    "adult, 0, false",
    "adult, 1, true",
    "adult, 3, true",
    "elder, 1, false",
    "elder, 2, true",
    "missing, 15, false"
  })
  @DisplayName("a skill opens at its stage and stays shut below it or when the stage is unknown")
  void gate(final String stage, final int rank, final boolean open) {
    assertEquals(open, StageGate.open(StageGate.needed(STAGES, gated(stage)), rank));
  }

  @ParameterizedTest(name = "needs {0}: rank {1}")
  @CsvSource({"adult, 1", "elder, 2", "missing, 2147483647"})
  @DisplayName("the rank a skill needs is read from its stage")
  void needed(final String stage, final int rank) {
    assertEquals(OptionalInt.of(rank), StageGate.needed(STAGES, gated(stage)));
  }
}
