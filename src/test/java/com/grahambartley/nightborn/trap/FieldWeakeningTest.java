package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FieldWeakeningTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"source\":\"consecrated\",\"cue\":\"nightborn.faith.consecrated\"}; false",
        "{\"source\":\"consecrated\"}; true",
        "{\"cue\":\"nightborn.faith.consecrated\"}; true",
        "{\"source\":\"\",\"cue\":\"nightborn.faith.consecrated\"}; true"
      })
  @DisplayName("a weakening ground names the weakening it gives and the line a player reads")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused,
        FieldWeakening.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
