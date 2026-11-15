package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillNamesTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "nightborn:core/thick_blood, skill.nightborn.core.thick_blood",
    "nightborn:noble/noble_blood, skill.nightborn.noble.noble_blood",
    "apack:fangs, skill.apack.fangs"
  })
  @DisplayName("a skill's name and description keys follow its id")
  void skill(final String id, final String key) {
    assertEquals(key, SkillNames.skill(Identifier.of(id)));
    assertEquals(key + ".description", SkillNames.description(Identifier.of(id)));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"nightborn:noble, skill_branch.nightborn.noble", "apack:hex, skill_branch.apack.hex"})
  @DisplayName("a branch's name key follows its id")
  void branch(final String id, final String key) {
    assertEquals(key, SkillNames.branch(Identifier.of(id)));
  }
}
