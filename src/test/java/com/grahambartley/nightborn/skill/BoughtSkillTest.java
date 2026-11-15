package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BoughtSkillTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"skill\":\"nightborn:core/thick_blood\",\"paid\":1}|true",
        "{\"skill\":\"nightborn:core/thick_blood\",\"paid\":0}|true",
        "{\"skill\":\"nightborn:core/thick_blood\",\"paid\":-1}|false",
        "{\"paid\":1}|false"
      })
  @DisplayName("a saved purchase loads only with a skill and a paid amount of zero or more")
  void parses(final String json, final boolean valid) {
    assertEquals(
        valid, BoughtSkill.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @Test
  @DisplayName("a purchase round-trips")
  void roundTrips() {
    final BoughtSkill bought = new BoughtSkill(Identifier.of("nightborn", "noble/noble_blood"), 3);
    assertTrue(
        BoughtSkill.CODEC
            .parse(
                JsonOps.INSTANCE,
                BoughtSkill.CODEC.encodeStart(JsonOps.INSTANCE, bought).getOrThrow())
            .getOrThrow()
            .equals(bought));
  }
}
