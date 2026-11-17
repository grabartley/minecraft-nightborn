package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StageSkillsTest {

  private static StageSkills parse(final String json) {
    return StageSkills.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"nightborn:core/blood_rush, 6", "nightborn:core/thick_blood, 2", "x:y, 2"})
  @DisplayName("a skill named in the weights uses its own weight, others the other weight")
  void weight(final String skill, final int weight) {
    final StageSkills skills =
        parse(
            "{\"count\": 2, \"weights\": {\"nightborn:core/blood_rush\": 6}, \"other_weight\": 2}");
    assertEquals(weight, skills.weight(Identifier.of(skill)));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"count\": 0} | true",
        "{\"count\": 64} | true",
        "{\"count\": 65} | false",
        "{\"count\": -1} | false",
        "{\"count\": 2, \"weights\": {\"a:b\": 1001}} | false",
        "{\"weights\": {}} | false"
      })
  @DisplayName("a stage picks between 0 and 64 skills with weights up to 1000")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, StageSkills.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "every {0}, {1} open, rolled {2}: wants {3}")
  @CsvSource({
    "false, 4, 0, 0",
    "false, 4, 2, 2",
    "false, 4, 4, 3",
    "false, 4, 9, 3",
    "false, 4, -1, 0",
    "false, 0, 1, 0",
    "true, 4, 0, 4",
    "true, 30, 2, 30",
    "true, 0, 5, 0"
  })
  @DisplayName("a stage never wants every skill unless it is the one that holds them all")
  void wanted(final boolean every, final int open, final int rolled, final int wanted) {
    final StageSkills skills = parse("{\"count\": 0, \"every\": " + every + "}");
    assertEquals(wanted, skills.wanted(open, rolled));
    assertEquals(every, skills.every());
  }
}
