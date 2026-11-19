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
          "summon",
          "guard",
          "break_away",
          "stagger",
          "takeoff",
          "fly_land",
          "form_downed",
          "form_fed",
          "rise",
          48,
          "death",
          60);

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
                        + "\"downed_form\":\"form_downed\",\"fed_form\":\"form_fed\","
                        + "\"rise\":\"rise\",\"rise_ticks\":48,\"death\":\"death\",\"death_ticks\":60}"))
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

  @ParameterizedTest(name = "fed {0}, fed form \"{1}\": kneels in \"{2}\"")
  @CsvSource(
      value = {"true, form_fed, form_fed", "false, form_fed, form_downed", "true, '', form_downed"})
  @DisplayName("on his knees he shows the fed form while bitten, if the fight has one")
  void floor(final boolean fed, final String fedForm, final String shown) {
    assertEquals(
        shown,
        new FightClips("", "", "", "", "", "", "form_downed", fedForm, "", 0, "", 0).floor(fed));
  }

  @ParameterizedTest(name = "death \"{0}\" for {1} ticks: holds {2}")
  @CsvSource(value = {"death, 60, 60", "'', 60, 0", "death, 0, 0"})
  @DisplayName("a death clip holds the body for its ticks, and no clip holds nothing")
  void deathHold(final String death, final int ticks, final int hold) {
    final FightClips clips = new FightClips("", "", "", "", "", "", "", "", "", 0, death, ticks);
    assertEquals(hold, clips.deathHold());
    assertEquals(new ShownForm("", "", death, false), clips.dying());
  }
}
