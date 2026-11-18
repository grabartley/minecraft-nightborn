package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StageAgeTest {

  static StageAge parse(final String json) {
    return StageAge.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"fledgling", "adult", "elder", "ancient"})
  @DisplayName("a stage open to players carries its name")
  void names(final String stage) {
    final StageAge age = parse("{\"name\":{\"translate\":\"nightborn.stage." + stage + "\"}}");
    assertEquals(Text.translatable("nightborn.stage." + stage), age.name());
    assertFalse(age.deed().isPresent());
  }

  @Test
  @DisplayName("a deed is optional, so the first stage needs none")
  void deedIsOptional() {
    final StageAge age =
        parse(
            "{\"name\":\"Adult\",\"deed\":{\"act\":\"drain\",\"victims\":\"nightborn:deed/adult\","
                + "\"description\":\"Drain\"}}");
    assertTrue(age.deed().isPresent());
    assertEquals(StageDeed.Act.DRAIN, age.deed().orElseThrow().act());
  }

  @Test
  @DisplayName("a stage with no name is refused")
  void needsAName() {
    assertTrue(StageAge.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).isError());
  }

  @ParameterizedTest(name = "{0} gives {1}")
  @CsvSource({
    "'{\"name\":\"Fledgling\"}', 1.0",
    "'{\"name\":\"Adult\",\"notoriety\":1.25}', 1.25",
    "'{\"name\":\"Ancient\",\"notoriety\":2}', 2.0"
  })
  @DisplayName("a stage weighs its kills on notoriety, plainly when it says nothing")
  void notoriety(final String json, final double weight) {
    assertEquals(weight, parse(json).notoriety());
  }

  @Test
  @DisplayName("a stage cannot take notoriety away")
  void notorietyIsNeverNegative() {
    assertTrue(
        StageAge.CODEC
            .parse(
                JsonOps.INSTANCE, JsonParser.parseString("{\"name\":\"Adult\",\"notoriety\":-1}"))
            .isError());
  }
}
