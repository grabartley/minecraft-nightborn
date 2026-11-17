package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseLookTest {

  static final PhaseLook BEAST =
      new PhaseLook(
          "beast_idle",
          "beast_walk",
          "form_beast",
          "fly",
          "form_fly",
          "roar_beast",
          Optional.empty());

  @Test
  @DisplayName("a phase's look reads its clips and held item from data, and defaults to plain")
  void parses() {
    assertEquals(
        new PhaseLook(
            "blood_idle",
            "blood_walk",
            "form_blood",
            "",
            "",
            "roar_blood",
            Optional.of(Identifier.ofVanilla("stick"))),
        PhaseLook.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"idle\":\"blood_idle\",\"walk\":\"blood_walk\",\"form\":\"form_blood\","
                        + "\"enter_clip\":\"roar_blood\",\"held\":\"minecraft:stick\"}"))
            .getOrThrow());
    assertEquals(
        PhaseLook.PLAIN,
        PhaseLook.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @ParameterizedTest(name = "flying {0}, downed {1}: idle {2}, walk {3}, form {4}")
  @CsvSource(
      value = {
        "false, false, beast_idle, beast_walk, form_beast",
        "true, false, fly, fly, form_fly",
        "false, true, '', '', form_downed",
        "true, true, '', '', form_downed"
      })
  @DisplayName("the look shown follows the ground, the air and the floor it lies on")
  void shows(
      final boolean flying,
      final boolean downed,
      final String idle,
      final String walk,
      final String form) {
    assertEquals(
        new ShownForm(idle, walk, form, false), BEAST.shown(flying, downed, "form_downed"));
  }

  @Test
  @DisplayName("a phase with no flying or downed clips keeps its own form")
  void fallsBackToItsForm() {
    final PhaseLook court =
        new PhaseLook("idle_ancient", "court_walk", "form_court", "", "", "", Optional.empty());
    assertEquals(
        new ShownForm("idle_ancient", "court_walk", "form_court", false),
        court.shown(true, false, ""));
    assertEquals(new ShownForm("", "", "form_court", false), court.shown(false, true, ""));
  }

  @ParameterizedTest(name = "enter clip \"{0}\": plays {1}")
  @CsvSource(value = {"roar_beast, roar_beast", "'', hiss"})
  @DisplayName("a phase with no clip of its own falls back to the fight's change clip")
  void enters(final String clip, final String plays) {
    assertEquals(plays, new PhaseLook("", "", "", "", "", clip, Optional.empty()).enters("hiss"));
  }
}
