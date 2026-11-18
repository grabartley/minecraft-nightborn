package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.faction.Faction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillTreeTest {

  @ParameterizedTest(name = "{0} spends in the {1} tree")
  @CsvSource({"VAMPIRE, VAMPIRE", "HUNTER, HUMAN"})
  @DisplayName("vampires spend vampire points and humans hunter points")
  void treeForFaction(final SkillTree tree, final Faction faction) {
    assertEquals(tree, SkillTree.of(faction));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"VAMPIRE, nightborn.skill_tree.vampire", "HUNTER, nightborn.skill_tree.hunter"})
  @DisplayName("each tree has a stable name key")
  void translationKey(final SkillTree tree, final String key) {
    assertEquals(key, tree.translationKey());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "VAMPIRE, nightborn.skill_tree.vampire.lines",
    "HUNTER, nightborn.skill_tree.hunter.lines"
  })
  @DisplayName("each tree names its choices, bloodlines or paths")
  void linesKey(final SkillTree tree, final String key) {
    assertEquals(key, tree.linesKey());
  }

  @ParameterizedTest(name = "{0} is at revision {1}")
  @CsvSource({"VAMPIRE, 0", "HUNTER, 1"})
  @DisplayName("the hunter tree moved on when it split into paths")
  void revision(final SkillTree tree, final int revision) {
    assertEquals(revision, tree.revision());
  }
}
