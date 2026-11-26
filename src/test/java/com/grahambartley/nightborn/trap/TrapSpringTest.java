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
class TrapSpringTest {

  @ParameterizedTest(name = "{0}: refused {1}, count {2}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"sound\":\"minecraft:block.iron_trapdoor.close\",\"particle\":{\"type\":\"minecraft:crit\"}}; false; 8",
        "{\"sound\":\"minecraft:entity.firework_rocket.blast\",\"particle\":{\"type\":\"minecraft:flash\"},\"count\":2}; false; 2",
        "{\"particle\":{\"type\":\"minecraft:crit\"}}; true; 0",
        "{\"sound\":\"minecraft:block.iron_trapdoor.close\",\"particle\":{\"type\":\"minecraft:crit\"},\"count\":65}; true; 0",
        "{\"sound\":{\"id\":\"minecraft:block.iron_trapdoor.close\",\"volume\":0.6},\"particle\":{\"type\":\"minecraft:crit\"}}; false; 8"
      })
  @DisplayName("a trap springs with a sound, bare or tuned, and a burst of particles")
  void reads(final String json, final boolean refused, final int count) {
    final var read = TrapSpring.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    assertEquals(refused, read.isError());
    if (!refused) {
      assertEquals(count, read.getOrThrow().count());
    }
  }
}
