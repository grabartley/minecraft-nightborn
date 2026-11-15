package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VampireStageTest {

  @Test
  @DisplayName("a stage needs only a rank and a weight")
  void smallest() {
    final VampireStage stage =
        VampireStage.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"rank\": 1, \"weight\": 5}"))
            .getOrThrow();
    assertEquals(StageBody.SAME, stage.body());
    assertEquals(StageSkills.NONE.weights(), stage.skills().weights());
    assertTrue(stage.loot().isEmpty());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"rank\": 15, \"weight\": 0} | true",
        "{\"rank\": 16, \"weight\": 1} | false",
        "{\"rank\": 1, \"weight\": 1001} | false",
        "{\"weight\": 1} | false",
        "{\"rank\": 1, \"weight\": 1, \"loot\": \"nightborn:entities/vampire/elder\"} | true",
        "{\"rank\": 1, \"weight\": 1, \"loot\": \"Not An Id\"} | false"
      })
  @DisplayName("a stage's rank, weight and loot table are checked")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads,
        VampireStage.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
