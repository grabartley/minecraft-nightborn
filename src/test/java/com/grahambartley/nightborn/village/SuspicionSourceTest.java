package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SuspicionSourceTest {

  @ParameterizedTest(name = "amount {0} loads: {1}")
  @CsvSource({"0, true", "25, true", "100, true", "-1, false", "101, false"})
  @DisplayName("a data pack may set any amount from 0 to 100")
  void readsAmounts(final int amount, final boolean loads) {
    assertEquals(
        loads,
        SuspicionSource.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"amount\": " + amount + "}"))
            .result()
            .isPresent());
  }
}
