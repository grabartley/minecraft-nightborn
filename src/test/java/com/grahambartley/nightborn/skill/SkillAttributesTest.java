package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillAttributesTest {

  @ParameterizedTest(name = "{0} #{1} -> {2}")
  @CsvSource({
    "nightborn:core/thick_blood, 0, nightborn:skill/core/thick_blood/0",
    "nightborn:core/thick_blood, 1, nightborn:skill/core/thick_blood/1",
    "apack:fangs, 0, apack:skill/fangs/0"
  })
  @DisplayName("each attribute of each skill gets its own modifier id, in the skill's namespace")
  void modifierIds(final String skill, final int index, final String modifier) {
    assertEquals(Identifier.of(modifier), SkillAttributes.modifierId(Identifier.of(skill), index));
  }
}
