package com.grahambartley.nightborn.mob.stage;

import static com.grahambartley.nightborn.mob.stage.StageFixtures.FAITH;
import static com.grahambartley.nightborn.mob.stage.StageFixtures.NOBLE_BLOOD;
import static com.grahambartley.nightborn.mob.stage.StageFixtures.RALLY;
import static com.grahambartley.nightborn.mob.stage.StageFixtures.RUSH;
import static com.grahambartley.nightborn.mob.stage.StageFixtures.STRENGTH;
import static com.grahambartley.nightborn.mob.stage.StageFixtures.THICK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SkillPicksTest {

  private static final Map<Identifier, Skill> VAMPIRE =
      SkillPicks.ofTree(StageFixtures.SKILLS, StageFixtures.BRANCHES, SkillTree.VAMPIRE);

  @Test
  @DisplayName("only skills in the vampire tree are on offer, from every bloodline")
  void ofTree() {
    assertEquals(Set.of(THICK, RUSH, STRENGTH, NOBLE_BLOOD, RALLY), VAMPIRE.keySet());
    assertEquals(
        Set.of(FAITH),
        SkillPicks.ofTree(StageFixtures.SKILLS, StageFixtures.BRANCHES, SkillTree.HUNTER).keySet());
  }

  @ParameterizedTest(name = "stage names the mob-only skill: {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a skill only vampire mobs can hold is on offer only to a stage that names it")
  void forStage(final boolean named) {
    final Map<Identifier, Integer> weights = named ? Map.of(RALLY, 5) : Map.of(THICK, 5);
    final Set<Identifier> expected =
        named
            ? Set.of(THICK, RUSH, STRENGTH, NOBLE_BLOOD, RALLY)
            : Set.of(THICK, RUSH, STRENGTH, NOBLE_BLOOD);
    assertEquals(
        expected,
        SkillPicks.forStage(
                VAMPIRE, StageFixtures.BRANCHES, StageFixtures.weights(weights, 1), skill -> true)
            .keySet());
  }

  @ParameterizedTest(name = "stage rank {0} reaches the gated skill: {1}")
  @CsvSource({"0, false", "1, false", "2, true", "3, true"})
  @DisplayName("a skill that needs a stage is on offer only to a stage that has reached it")
  void gated(final int rank, final boolean offered) {
    assertEquals(
        offered,
        SkillPicks.forStage(
                VAMPIRE,
                StageFixtures.BRANCHES,
                StageFixtures.weights(Map.of(), 1),
                skill -> skill != VAMPIRE.get(RUSH) || rank >= 2)
            .containsKey(RUSH));
  }

  @ParameterizedTest(name = "wants {0} of {1}: {2}")
  @CsvSource({"0, 4, 0", "2, 4, 2", "3, 4, 3", "4, 4, 3", "9, 4, 3", "-1, 4, 0", "1, 0, 0"})
  @DisplayName("a vampire mob never holds every skill, which only the Eldest does")
  void most(final int wanted, final int treeSize, final int most) {
    assertEquals(most, SkillPicks.most(wanted, treeSize));
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
  @DisplayName("a skill is picked only once the skills it needs are held")
  void respectsRequires(final long seed) {
    final Random random = new Random(seed);
    final List<Identifier> picks =
        SkillPicks.pick(VAMPIRE, StageFixtures.weights(Map.of(), 1), 3, random::nextDouble);
    assertEquals(3, picks.size());
    for (int i = 0; i < picks.size(); i++) {
      final Skill skill = VAMPIRE.get(picks.get(i));
      assertTrue(picks.subList(0, i).containsAll(skill.requires()), picks.toString());
    }
  }

  @Test
  @DisplayName("a heavily weighted skill is taken as soon as it is open")
  void weights() {
    assertEquals(
        List.of(THICK, RUSH),
        SkillPicks.pick(
            VAMPIRE, StageFixtures.weights(Map.of(THICK, 1000, RUSH, 1000), 0), 2, () -> 0.5));
  }

  @Test
  @DisplayName("skills weighted 0 are never picked, so a vampire may end with fewer")
  void zeroWeight() {
    assertEquals(
        List.of(NOBLE_BLOOD),
        SkillPicks.pick(VAMPIRE, StageFixtures.weights(Map.of(NOBLE_BLOOD, 1), 0), 3, () -> 0.5));
  }

  @Test
  @DisplayName("taking every skill takes the whole tree, each after the skills it needs")
  void every() {
    final List<Identifier> picks = SkillPicks.every(VAMPIRE);
    assertEquals(VAMPIRE.keySet(), Set.copyOf(picks));
    assertEquals(VAMPIRE.size(), picks.size());
    for (int i = 0; i < picks.size(); i++) {
      assertTrue(
          picks.subList(0, i).containsAll(VAMPIRE.get(picks.get(i)).requires()), picks.toString());
    }
  }
}
