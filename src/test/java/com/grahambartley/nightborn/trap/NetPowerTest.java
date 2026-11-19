package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class NetPowerTest {

  @ParameterizedTest(name = "range {0}, speed {1} -> flies {2} ticks")
  @CsvSource({"24, 2, 12", "24, 1.5, 16", "10, 3, 4"})
  @DisplayName("a net flies only as long as it takes to cover its range")
  void flies(final float range, final float speed, final int ticks) {
    final NetPower net =
        NetPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"range\":"
                        + range
                        + ",\"speed\":"
                        + speed
                        + ",\"ground_ticks\":100,\"damage_type\":\"nightborn:silver\"}"))
            .getOrThrow();
    assertEquals(ticks, net.flightTicks());
    assertEquals(100, net.groundTicks());
  }

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":24,\"speed\":2,\"ground_ticks\":100,\"damage_type\":\"nightborn:silver\"}; false",
        "{\"range\":24,\"speed\":2,\"ground_ticks\":0,\"damage_type\":\"nightborn:silver\"}; true",
        "{\"range\":24,\"speed\":0,\"ground_ticks\":100,\"damage_type\":\"nightborn:silver\"}; true",
        "{\"range\":24,\"speed\":2,\"ground_ticks\":100}; true"
      })
  @DisplayName("a net needs a range, a speed, a time on the ground and a damage type")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, NetPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
