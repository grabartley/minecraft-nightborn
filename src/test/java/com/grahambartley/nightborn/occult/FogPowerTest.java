package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.effect.StatusEffects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FogPowerTest {

  @Test
  @DisplayName("a fog reads its radius, how long it hangs and what it does to those inside")
  void parses() {
    final FogPower fog =
        FogPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"radius\":4,\"duration_ticks\":160,\"effects\":[{\"id\":\"minecraft:blindness\",\"duration\":60}]}"))
            .getOrThrow();
    assertEquals(4.0F, fog.radius());
    assertEquals(160, fog.durationTicks());
    assertEquals(StatusEffects.BLINDNESS, fog.effects().get(0).getEffectType());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"radius\":0,\"duration_ticks\":160,\"effects\":[]}",
        "{\"radius\":40,\"duration_ticks\":160,\"effects\":[]}",
        "{\"radius\":4,\"duration_ticks\":5,\"effects\":[]}",
        "{\"radius\":4,\"duration_ticks\":160}"
      })
  @DisplayName("a fog out of bounds or without effects is rejected")
  void rejects(final String json) {
    assertTrue(FogPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
