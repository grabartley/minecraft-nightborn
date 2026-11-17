package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.stage.StageAge;
import com.grahambartley.nightborn.mob.stage.StageBody;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.mob.stage.StageLook;
import com.grahambartley.nightborn.mob.stage.StageSkills;
import com.grahambartley.nightborn.mob.stage.VampireStage;
import java.util.Map;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeLadderTest {

  static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  static VampireStage stage(final int rank, final boolean forPlayers) {
    return new VampireStage(
        rank,
        1,
        StageBody.SAME,
        StageSkills.NONE,
        StageCombat.PLAIN,
        StageLook.PLAIN,
        Optional.empty(),
        forPlayers
            ? Optional.of(new StageAge(Text.literal("stage " + rank), Optional.empty()))
            : Optional.empty());
  }

  static final AgeLadder LADDER =
      AgeLadder.of(
          Map.of(
              id("ancient"), stage(3, true),
              id("lord"), stage(4, false),
              id("fledgling"), stage(0, true),
              id("elder"), stage(2, true),
              id("adult"), stage(1, true)));

  @ParameterizedTest(name = "rung {0} is {1}")
  @CsvSource({"0, fledgling", "1, adult", "2, elder", "3, ancient"})
  @DisplayName("the ladder is the stages open to players, youngest first")
  void orders(final int rank, final String path) {
    assertEquals(4, LADDER.size());
    assertEquals(id(path), LADDER.rung(rank).orElseThrow().id());
    assertEquals(Optional.of(rank), LADDER.rankOf(id(path)));
  }

  @ParameterizedTest(name = "rung {0}")
  @CsvSource({"-1", "4", "99"})
  @DisplayName("there is no rung off either end")
  void noRungOffTheEnds(final int rank) {
    assertEquals(Optional.empty(), LADDER.rung(rank));
  }

  @Test
  @DisplayName("a stage only mobs have is not on the ladder")
  void leavesOutMobStages() {
    assertEquals(Optional.empty(), LADDER.rankOf(id("lord")));
    assertFalse(LADDER.rungs().stream().anyMatch(rung -> rung.stage().rank() == 4));
  }

  @ParameterizedTest(name = "rung {0} pays as stage {1}")
  @CsvSource({"0, 0", "2, 2", "3, 3", "9, 3"})
  @DisplayName("a player pays hunters as the stage they stand on")
  void paysAsItsStage(final int rank, final int stageRank) {
    assertEquals(stageRank, LADDER.stageRank(rank));
    assertTrue(AgeLadder.of(Map.of()).stageRank(rank) == 0);
  }
}
