package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StageDataCheckTest {

  private static final Identifier ELDER = Identifier.of("nightborn", "elder");

  private static VampireStage elder(final int count, final Map<Identifier, Integer> weights) {
    return elder(count, weights, false);
  }

  private static VampireStage elder(
      final int count, final Map<Identifier, Integer> weights, final boolean every) {
    return new VampireStage(
        2,
        10,
        StageBody.SAME,
        new StageSkills(ConstantIntProvider.create(count), weights, 1, every),
        StageCombat.PLAIN,
        StageLook.PLAIN,
        Optional.empty());
  }

  static Stream<Arguments> stages() {
    return Stream.of(
        Arguments.of(
            "good stages", Map.of(ELDER, elder(3, Map.of(StageFixtures.RUSH, 4))), List.of()),
        Arguments.of("no stages", Map.of(), List.of("there are no vampire stages")),
        Arguments.of(
            "no weight",
            Map.of(ELDER, StageFixtures.stage(2, 0)),
            List.of("no vampire stage has any weight")),
        Arguments.of(
            "a skill outside the tree",
            Map.of(ELDER, elder(1, Map.of(StageFixtures.FAITH, 3))),
            List.of("nightborn:elder weighs nightborn:hunter/faith, which is not a vampire skill")),
        Arguments.of(
            "a stage wanting every skill",
            Map.of(ELDER, elder(4, Map.of())),
            List.of("nightborn:elder may want every vampire skill, which only the Eldest has")),
        Arguments.of(
            "a stage that says it holds every skill",
            Map.of(ELDER, elder(0, Map.of(), true)),
            List.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("stages")
  @DisplayName("stage data raises a problem for each thing a data pack got wrong")
  void problems(
      final String name, final Map<Identifier, VampireStage> stages, final List<String> problems) {
    assertEquals(
        problems,
        StageDataCheck.problems(
            stages,
            SkillPicks.ofTree(StageFixtures.SKILLS, StageFixtures.BRANCHES, SkillTree.VAMPIRE),
            StageFixtures.BRANCHES));
  }
}
