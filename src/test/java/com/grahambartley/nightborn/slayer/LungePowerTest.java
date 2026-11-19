package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LungePowerTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"distance\":5,\"stake_share\":0.5,\"downs\":{\"below\":0.4,\"up_to\":\"nightborn:elder\"}} | 5 | 0.5 | 0.4",
        "{\"distance\":3,\"stake_share\":0,\"downs\":{\"below\":0.1,\"up_to\":\"nightborn:adult\"}} | 3 | 0 | 0.1"
      })
  @DisplayName("a lunge reads its reach, the extra share of a stake's blow and when it downs")
  void reads(final String json, final float distance, final float share, final float below) {
    final LungePower power =
        LungePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(distance, power.distance());
    assertEquals(share, power.stakeShare());
    assertEquals(below, power.downs().below());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"distance\":5,\"stake_share\":0.5}",
        "{\"distance\":0,\"stake_share\":0.5,\"downs\":{\"below\":0.4,\"up_to\":\"nightborn:elder\"}}"
      })
  @DisplayName("a lunge out of range or without its downing rule is refused")
  void refuses(final String json) {
    assertTrue(LungePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
