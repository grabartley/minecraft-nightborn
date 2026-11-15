package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SkillAttributesTest {

  static final SkillAttribute ARMOUR =
      new SkillAttribute(
          EntityAttributes.GENERIC_ARMOR, 2.0, EntityAttributeModifier.Operation.ADD_VALUE);
  static final SkillAttribute HEALTH =
      new SkillAttribute(
          EntityAttributes.GENERIC_MAX_HEALTH,
          0.1,
          EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

  @ParameterizedTest(name = "{0} #{1} -> {2}")
  @CsvSource({
    "nightborn:core/thick_blood, 0, nightborn:skill/nightborn/core/thick_blood/0",
    "nightborn:core/thick_blood, 1, nightborn:skill/nightborn/core/thick_blood/1",
    "apack:fangs, 0, nightborn:skill/apack/fangs/0"
  })
  @DisplayName(
      "each attribute of each skill gets its own modifier id, always in this mod's namespace")
  void modifierIds(final String skill, final int index, final String modifier) {
    assertEquals(Identifier.of(modifier), SkillAttributes.modifierId(Identifier.of(skill), index));
  }

  @Test
  @DisplayName("a skill's attributes map to one modifier each")
  void modifiers() {
    assertEquals(
        Map.of(
            Identifier.of("nightborn", "skill/nightborn/core/x/0"), ARMOUR,
            Identifier.of("nightborn", "skill/nightborn/core/x/1"), HEALTH),
        SkillAttributes.modifiers(Identifier.of("nightborn", "core/x"), List.of(ARMOUR, HEALTH)));
  }

  @ParameterizedTest(name = "{0} on armour -> stray {1}")
  @CsvSource({
    "nightborn:skill/nightborn/noble/noble_blood/0, false",
    "nightborn:skill/nightborn/noble/removed/0, true",
    "nightborn:skill/nightborn/noble/noble_blood/1, true",
    "nightborn:vampire_health, false",
    "minecraft:armor.chestplate, false",
    "othermod:skill/fangs/0, false"
  })
  @DisplayName(
      "a skill modifier that no owned skill gives is stray, and other mods' modifiers are left alone")
  void stray(final String modifier, final boolean stray) {
    final Map<Identifier, SkillAttribute> wanted =
        Map.of(Identifier.of("nightborn", "skill/nightborn/noble/noble_blood/0"), ARMOUR);
    assertEquals(
        stray,
        SkillAttributes.stray(Identifier.of(modifier), EntityAttributes.GENERIC_ARMOR, wanted));
  }

  @Test
  @DisplayName("a wanted modifier found on the wrong attribute is stray there")
  void wrongAttribute() {
    final Identifier id = Identifier.of("nightborn", "skill/nightborn/noble/noble_blood/0");
    assertEquals(
        true, SkillAttributes.stray(id, EntityAttributes.GENERIC_MAX_HEALTH, Map.of(id, ARMOUR)));
  }
}
