package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FightClipsTest {

  static final FightClips CLIPS =
      new FightClips(
          "summon", "guard", "break_away", "stagger", "takeoff", "fly_land", "form_downed");

  @Test
  @DisplayName("the fight's clips read from data, and a fight may name none")
  void parses() {
    assertEquals(
        CLIPS,
        FightClips.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"summon\":\"summon\",\"guard\":\"guard\",\"break_away\":\"break_away\","
                        + "\"stagger\":\"stagger\",\"takeoff\":\"takeoff\",\"land\":\"fly_land\","
                        + "\"downed_form\":\"form_downed\"}"))
            .getOrThrow());
    assertEquals(
        FightClips.NONE,
        FightClips.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @ParameterizedTest(name = "was flying {0}, flying {1}: plays \"{2}\"")
  @CsvSource(
      value = {
        "false, true, takeoff",
        "true, false, fly_land",
        "true, true, ''",
        "false, false, ''"
      })
  @DisplayName("taking off and landing each play their clip once, on the change")
  void flightEdge(final boolean wasFlying, final boolean flying, final String plays) {
    assertEquals(plays, FightClips.flightEdge(wasFlying, flying, CLIPS));
  }
}
