package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RareSpreadPlacementTest {

  private static final Path CAMP_SET =
      Path.of("src/main/resources/data/nightborn/worldgen/structure_set/hunter_camp.json");

  private static RareSpreadPlacement parse(final String json) {
    return RareSpreadPlacement.CODEC
        .codec()
        .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
        .getOrThrow();
  }

  @Test
  void theHunterCampSetScalesWithTheCampSetting() throws IOException {
    final RareSpreadPlacement placement =
        parse(
            JsonParser.parseString(Files.readString(CAMP_SET))
                .getAsJsonObject()
                .getAsJsonObject("placement")
                .toString());
    assertEquals(StructureRarity.HUNTER_CAMPS, placement.rarity());
  }

  @ParameterizedTest(name = "spacing {0}, separation {1} loads: {2}")
  @CsvSource({"48, 20, true", "20, 20, false", "10, 30, false"})
  void refusesASeparationAsWideAsItsSpacing(
      final int spacing, final int separation, final boolean loads) {
    final String json =
        "{\"salt\":1,\"rarity\":\"hunter_camps\",\"spacing\":"
            + spacing
            + ",\"separation\":"
            + separation
            + "}";
    assertEquals(
        loads,
        RareSpreadPlacement.CODEC
            .codec()
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .isSuccess());
  }

  @Test
  void needsToBeToldWhichRarityItReads() {
    assertTrue(
        RareSpreadPlacement.CODEC
            .codec()
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"salt\":1,\"spacing\":48,\"separation\":20}"))
            .isError());
  }
}
