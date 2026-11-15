package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SkillAttributeTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"attribute\":\"minecraft:generic.armor\",\"amount\":2}|ADD_VALUE",
        "{\"attribute\":\"minecraft:generic.armor\",\"amount\":2,\"operation\":\"add_multiplied_total\"}|ADD_MULTIPLIED_TOTAL"
      })
  @DisplayName("an attribute reads its amount and defaults to adding a flat value")
  void parses(final String json, final EntityAttributeModifier.Operation operation) {
    final SkillAttribute attribute =
        SkillAttribute.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(EntityAttributes.GENERIC_ARMOR, attribute.attribute());
    assertEquals(2.0, attribute.amount());
    assertEquals(operation, attribute.operation());
  }

  @Test
  @DisplayName("an unknown attribute is rejected")
  void rejectsUnknown() {
    assertFalse(
        SkillAttribute.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"attribute\":\"minecraft:nope\",\"amount\":1}"))
            .isSuccess());
  }
}
