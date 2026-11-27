package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.registry.DynamicRegistryManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BodyZoneDataTest {

  private static final Path ZONES =
      Path.of("src/main/resources/data/nightborn/nightborn/body_zones");

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"humanoid"})
  @DisplayName("the shipped zone file loads and matches the built-in fallback")
  void shippedMatchesFallback(final String id) throws IOException {
    assertEquals(id, BodyZoneData.HUMANOID.getPath());
    assertEquals(
        BodyZones.DEFAULT,
        BodyZones.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(Files.readString(ZONES.resolve(id + ".json"))))
            .getOrThrow());
  }

  @Test
  @DisplayName("with no zone data loaded the built-in zones are used")
  void fallsBack() {
    assertEquals(BodyZones.DEFAULT, BodyZoneData.zones(DynamicRegistryManager.EMPTY));
  }
}
