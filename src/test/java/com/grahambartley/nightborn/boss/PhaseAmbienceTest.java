package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseAmbienceTest {

  static final PhaseAmbience.Cue BELL =
      new PhaseAmbience.Cue(
          Optional.of(Identifier.ofVanilla("block.bell.resonate")), 3, 1.0F, 0.5F, false);
  static final PhaseAmbience.Cue BOLT =
      new PhaseAmbience.Cue(Optional.empty(), 1, 1.0F, 1.0F, true);
  static final PhaseAmbience AMBIENCE = new PhaseAmbience(400, 900, List.of(BELL, BOLT));

  @ParameterizedTest(name = "a roll of {0}: {1} ticks on")
  @CsvSource({"0, 400", "250, 650", "500, 900", "501, 400", "-1, 900"})
  @DisplayName("the next cue comes no sooner than the shortest wait and no later than the longest")
  void nextAt(final int roll, final long wait) {
    assertEquals(1000L + wait, AMBIENCE.nextAt(1000L, roll));
  }

  @ParameterizedTest(name = "a roll of {0}: lightning {1}")
  @CsvSource({"0, false", "2, false", "3, true", "4, false", "7, true"})
  @DisplayName("cues are picked by weight")
  void pick(final int roll, final boolean lightning) {
    assertEquals(lightning, AMBIENCE.pick(roll).lightning());
    assertEquals(4, AMBIENCE.totalWeight());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"min_ticks\":400,\"max_ticks\":900,\"cues\":[{\"sound\":\"minecraft:block.bell.use\"}]} | true",
        "{\"min_ticks\":400,\"max_ticks\":400,\"cues\":[{\"lightning\":true}]} | true",
        "{\"min_ticks\":900,\"max_ticks\":400,\"cues\":[{\"lightning\":true}]} | false",
        "{\"min_ticks\":400,\"max_ticks\":900,\"cues\":[]} | false",
        "{\"min_ticks\":400,\"max_ticks\":900,\"cues\":[{}]} | false",
        "{\"min_ticks\":5,\"max_ticks\":900,\"cues\":[{\"lightning\":true}]} | false",
        "{\"min_ticks\":400,\"max_ticks\":900,\"cues\":[{\"lightning\":true,\"pitch\":3}]} | false"
      })
  @DisplayName("ambience needs a wait that makes sense and cues that do something")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads,
        PhaseAmbience.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
