package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StageLookTest {

  @ParameterizedTest(name = "{0} on {1}: {2}")
  @CsvSource({
    "nightborn:textures/entity/vampire/stage/elder, male, nightborn:textures/entity/vampire/stage/elder_male.png",
    "nightborn:textures/entity/vampire/stage/ancient, female, nightborn:textures/entity/vampire/stage/ancient_female.png"
  })
  @DisplayName("a stage's marks use one sheet per rig")
  void marksTexture(final String marks, final String rig, final String texture) {
    assertEquals(
        Optional.of(Identifier.of(texture)),
        new StageLook(Optional.empty(), Optional.of(Identifier.of(marks)), Optional.empty())
            .marksTexture(rig));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"male", "female"})
  @DisplayName("a stage with no marks draws nothing over the look")
  void noMarks(final String rig) {
    assertEquals(Optional.empty(), StageLook.PLAIN.marksTexture(rig));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"gray, GRAY", "gold, GOLD", "dark_purple, DARK_PURPLE", "dark_red, DARK_RED"})
  @DisplayName("a stage's name colour is read from its data")
  void nameColor(final String name, final Formatting colour) {
    assertEquals(
        Optional.of(TextColor.fromFormatting(colour)),
        StageLook.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"name_color\": \"" + name + "\"}"))
            .getOrThrow()
            .nameColor());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"{}", "{\"idle\": \"idle_elder\"}"})
  @DisplayName("a stage with no name colour leaves the normal colour")
  void noNameColor(final String json) {
    assertEquals(
        Optional.empty(),
        StageLook.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .nameColor());
  }
}
