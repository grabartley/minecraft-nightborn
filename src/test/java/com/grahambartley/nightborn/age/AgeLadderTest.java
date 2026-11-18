package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.stage.StageAge;
import com.grahambartley.nightborn.mob.stage.StageBody;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.mob.stage.StageLook;
import com.grahambartley.nightborn.mob.stage.StageSkills;
import com.grahambartley.nightborn.mob.stage.VampireStage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
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
            ? Optional.of(
                new StageAge(
                    Text.literal("stage " + rank), Optional.empty(), StageAge.PLAIN_NOTORIETY))
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

  @ParameterizedTest(name = "rung {0} weighs kills by {1}")
  @CsvSource({"0, 1.0", "1, 1.25", "2, 1.5", "3, 2.0", "9, 2.0"})
  @DisplayName("an older player's kills weigh more on their notoriety")
  void weighsNotoriety(final int rank, final double multiplier) {
    final double[] weights = {1.0, 1.25, 1.5, 2.0};
    final Map<Identifier, VampireStage> stages = new HashMap<>();
    for (int stage = 0; stage < weights.length; stage++) {
      stages.put(
          id("stage_" + stage),
          new VampireStage(
              stage,
              1,
              StageBody.SAME,
              StageSkills.NONE,
              StageCombat.PLAIN,
              StageLook.PLAIN,
              Optional.empty(),
              Optional.of(
                  new StageAge(Text.literal("stage " + stage), Optional.empty(), weights[stage]))));
    }
    assertEquals(multiplier, AgeLadder.of(stages).notoriety(rank));
    assertEquals(StageAge.PLAIN_NOTORIETY, AgeLadder.of(Map.of()).notoriety(rank));
  }

  @ParameterizedTest(name = "rung {0} is the top: {1}")
  @CsvSource({"3, true", "2, false", "0, false", "4, false"})
  @DisplayName("only the oldest stage open to players is the top")
  void top(final int rank, final boolean top) {
    assertEquals(top, LADDER.top(rank));
  }
}
