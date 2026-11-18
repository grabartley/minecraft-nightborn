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
        "{\"tree\":\"vampire\"}|VAMPIRE|false|0|true",
        "{\"tree\":\"vampire\",\"line\":true,\"order\":2}|VAMPIRE|true|2|true",
        "{\"tree\":\"hunter\",\"order\":0}|HUNTER|false|0|true",
        "{\"tree\":\"hunter\",\"line\":true,\"order\":3}|HUNTER|true|3|true",
        "{\"tree\":\"vampire\",\"order\":15,\"learnable\":false}|VAMPIRE|false|15|false"
      })
  @DisplayName(
      "a branch reads its tree, whether it is a bloodline or path, its order and who may learn it")
  void parses(
      final String json,
      final SkillTree tree,
      final boolean line,
      final int order,
      final boolean learnable) {
    assertEquals(
        new SkillBranch(tree, line, order, learnable),
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
