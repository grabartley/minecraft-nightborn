package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankDeedTest {

  @ParameterizedTest(name = "{0} at {1} done by {2} at stage {3} -> {4}")
  @CsvSource({
    "STAKE, 1, STAKE, 0, false",
    "STAKE, 1, STAKE, 1, true",
    "STAKE, 1, STAKE, 4, true",
    "STAKE, 0, CLEAR_HOUSEHOLD, 3, false",
    "CLEAR_HOUSEHOLD, 0, CLEAR_HOUSEHOLD, 0, true",
    "END_ELDEST, 0, END_ELDEST, 5, true",
    "END_ELDEST, 0, STAKE, 5, false"
  })
  @DisplayName("a deed is done by its own act on a vampire at least as old as it asks")
  void doneBy(
      final RankDeed.Act act,
      final int minimumStage,
      final RankDeed.Act done,
      final int stageRank,
      final boolean expected) {
    assertEquals(expected, RankFixtures.deed(act, minimumStage).doneBy(done, stageRank));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"act\":\"stake\",\"minimum_stage\":1,\"description\":\"x\"}|STAKE|1",
        "{\"act\":\"clear_household\",\"description\":\"x\"}|CLEAR_HOUSEHOLD|0",
        "{\"act\":\"end_eldest\",\"description\":\"x\"}|END_ELDEST|0"
      })
  @DisplayName("a deed reads its act and the youngest vampire it counts, none by default")
  void parses(final String json, final RankDeed.Act act, final int minimumStage) {
    final RankDeed deed =
        RankDeed.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(new RankDeed(act, minimumStage, Text.literal("x")), deed);
  }
}
