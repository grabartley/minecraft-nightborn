package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
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
        "{\"phases\":[{\"name\":\"court\",\"health\":30}],\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\",\"change_knock\":9} | false",
        "{\"phases\":[{\"name\":\"hunt\",\"health\":30}],\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\",\"wall\":{\"fog\":\"a:b\",\"shapes\":\"a:c\",\"palette\":[\"#FFFFFF\"],\"beat_ticks\":120,\"breathes\":true}} | true",
        "{\"phases\":[{\"name\":\"hunt\",\"health\":30}],\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\",\"wall\":{\"fog\":\"a:b\",\"shapes\":\"a:c\",\"palette\":[],\"beat_ticks\":120}} | false"
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

  @ParameterizedTest(name = "roll {0} with {1} lines says {2}")
  @CsvSource(
      delimiter = '|',
      value = {
        "0 | \"a\",\"b\",\"c\" | a",
        "1 | \"a\",\"b\",\"c\" | b",
        "5 | \"a\",\"b\",\"c\" | c",
        "-1 | \"a\",\"b\",\"c\" | c",
        "7 | '' | ''"
      })
  @DisplayName("a summoner hears one of the fight's lines, or none when it has none")
  void summonerLine(final int roll, final String lines, final String said) {
    final BossFightPlan plan =
        BossFightPlan.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    TWO_PHASES.substring(0, TWO_PHASES.length() - 1)
                        + (lines.isEmpty() ? "" : ",\"summoner_lines\":[" + lines + "]")
                        + "}"))
            .getOrThrow();
    assertEquals(said.isEmpty() ? Optional.empty() : Optional.of(said), plan.summonerLine(roll));
  }
}
