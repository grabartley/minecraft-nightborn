package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinecraftBootstrapExtension.class)
class StageDataCheckTest {

  private static final Identifier ELDER = Identifier.of("nightborn", "elder");

  private static VampireStage elder(final int count, final Map<Identifier, Integer> weights) {
    return new VampireStage(
        2,
        10,
        StageBody.SAME,
        new StageSkills(ConstantIntProvider.create(count), weights, 1),
        StageCombat.PLAIN,
        StageLook.PLAIN,
        Optional.empty());
  }

  private static List<String> problems(final Map<Identifier, VampireStage> stages) {
    return StageDataCheck.problems(
        stages, SkillPicks.ofTree(StageFixtures.SKILLS, StageFixtures.BRANCHES, SkillTree.VAMPIRE));
  }

  @Test
  @DisplayName("good stages raise nothing")
  void clean() {
    assertEquals(List.of(), problems(Map.of(ELDER, elder(3, Map.of(StageFixtures.RUSH, 4)))));
  }

  @Test
  @DisplayName("no stages, or no weight in any, is a problem")
  void empty() {
    assertEquals(List.of("there are no vampire stages"), problems(Map.of()));
    assertEquals(
        List.of("no vampire stage has any weight"),
        problems(Map.of(ELDER, StageFixtures.stage(2, 0))));
  }

  @Test
  @DisplayName("weighing a skill outside the vampire tree is a problem")
  void foreignSkill() {
    assertEquals(
        List.of("nightborn:elder weighs nightborn:hunter/faith, which is not a vampire skill"),
        problems(Map.of(ELDER, elder(1, Map.of(StageFixtures.FAITH, 3)))));
  }

  @Test
  @DisplayName("a stage that may want every vampire skill is a problem")
  void everySkill() {
    assertEquals(
        List.of("nightborn:elder may want every vampire skill, which only the Eldest has"),
        problems(Map.of(ELDER, elder(4, Map.of()))));
  }
}
