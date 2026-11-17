package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossFightPlanTest {

  private static final String TWO_PHASES =
      "{\"phases\":[{\"name\":\"court\",\"health\":30},{\"name\":\"blood\",\"health\":70}],"
          + "\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\"}";

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"phases\":[{\"name\":\"court\",\"health\":30}],\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\"} | true",
        "{\"phases\":[],\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\"} | false",
        "{\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\"} | false",
        "{\"phases\":[{\"name\":\"court\",\"health\":30}],\"change_sound\":\"minecraft:entity.warden.roar\"} | false",
        "{\"phases\":[{\"name\":\"court\",\"health\":30}],\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\",\"change_ticks\":601} | false",
        "{\"phases\":[{\"name\":\"court\",\"health\":30}],\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\",\"change_knock\":9} | false"
      })
  @DisplayName("a fight needs at least one phase and a clip and sound for the change between them")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads,
        BossFightPlan.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "phase {0} is {1}")
  @CsvSource({"-1, court", "0, court", "1, blood", "2, blood", "99, blood"})
  @DisplayName("a fight hands out its phases in order and never runs off either end")
  void phases(final int index, final String name) {
    final BossFightPlan plan =
        BossFightPlan.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(TWO_PHASES))
            .getOrThrow();
    assertEquals(name, plan.phase(index).name());
    assertEquals(List.of(30, 70), plan.shares());
    assertEquals(60, plan.changeTicks());
    assertEquals(1.5F, plan.changeKnock());
    assertEquals(6.0F, plan.changeRange());
  }

  @ParameterizedTest(name = "names a summon clip: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        ",\"clips\":{\"summon\":\"summon\",\"guard\":\"guard\"} | summon | guard",
        "'' | '' | ''"
      })
  @DisplayName("a fight reads the clips it names and plays none when it names none")
  void clips(final String extra, final String summon, final String guard) {
    final BossFightPlan plan =
        BossFightPlan.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    TWO_PHASES.substring(0, TWO_PHASES.length() - 1) + extra + "}"))
            .getOrThrow();
    assertEquals(summon, plan.clips().summon());
    assertEquals(guard, plan.clips().guard());
    assertEquals(extra.isEmpty(), plan.clips().equals(FightClips.NONE));
  }
}
