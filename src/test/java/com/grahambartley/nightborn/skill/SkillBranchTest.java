package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillBranchTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"tree\":\"vampire\"}|VAMPIRE|false|0",
        "{\"tree\":\"vampire\",\"bloodline\":true,\"order\":2}|VAMPIRE|true|2",
        "{\"tree\":\"hunter\",\"order\":1}|HUNTER|false|1"
      })
  @DisplayName("a branch reads its tree, whether it is a bloodline, and its order")
  void parses(final String json, final SkillTree tree, final boolean bloodline, final int order) {
    assertEquals(
        new SkillBranch(tree, bloodline, order),
        SkillBranch.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {"{}", "{\"tree\":\"werewolf\"}", "{\"tree\":\"vampire\",\"order\":16}"})
  @DisplayName("a branch with no tree, an unknown tree or an order out of range is rejected")
  void rejects(final String json) {
    assertEquals(
        false, SkillBranch.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
