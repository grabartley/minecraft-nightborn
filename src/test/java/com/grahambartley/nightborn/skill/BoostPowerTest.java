package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BoostPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"duration_ticks\":200,\"attributes\":[{\"attribute\":\"minecraft:generic.attack_damage\",\"amount\":3}],\"kill_ticks\":40,\"most_ticks\":400,\"rescue_below\":0.25}; false",
        "{\"duration_ticks\":200,\"attributes\":[]}; false",
        "{\"attributes\":[]}; true",
        "{\"duration_ticks\":200,\"attributes\":[],\"rescue_below\":1.5}; true"
      })
  @DisplayName("a boost needs a time and its attributes, and starts itself only below whole health")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, BoostPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "{0}: rescues {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"duration_ticks\":200,\"attributes\":[],\"rescue_below\":0.25}; true",
        "{\"duration_ticks\":200,\"attributes\":[]}; false"
      })
  @DisplayName("a boost may start itself when its owner is close to death")
  void rescues(final String json, final boolean rescues) {
    assertEquals(
        rescues,
        BoostPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .rescueBelow()
            .isPresent());
  }

  @ParameterizedTest(name = "{0}: downs {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"duration_ticks\":120,\"attributes\":[],\"downs\":{\"below\":0.3,\"up_to\":\"nightborn:elder\"}}; true",
        "{\"duration_ticks\":120,\"attributes\":[]}; false"
      })
  @DisplayName("a boost may down a badly hurt vampire its owner strikes")
  void downs(final String json, final boolean downs) {
    assertEquals(
        downs,
        BoostPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .downs()
            .isPresent());
  }
}
