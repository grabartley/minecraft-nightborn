package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LivesInLootConditionTest {

  private static final Path LOOT =
      Path.of("src/main/resources/data/nightborn/loot_table/entities/vampire");
  private static final Map<String, String> RELIC_HOMES =
      Map.of(
          "nightborn:castle_signet", "nightborn:castle",
          "nightborn:crypt_reliquary", "nightborn:crypt",
          "nightborn:manor_locket", "nightborn:ruined_manor",
          "nightborn:lair_fang", "nightborn:underground_lair");

  @Test
  @DisplayName("the condition is registered and reads the entity that died")
  void registered() {
    assertEquals(
        "nightborn:lives_in",
        Registries.LOOT_CONDITION_TYPE.getId(LivesInLootCondition.TYPE).toString());
    assertTrue(
        new LivesInLootCondition(null)
            .getRequiredParameters()
            .contains(LootContextParameters.THIS_ENTITY));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"elder", "ancient", "lord"})
  @DisplayName("an older vampire drops only the relic of the structure it lives in")
  void olderVampiresDropTheirHomesRelic(final String stage) throws IOException {
    final Map<String, String> homes = new HashMap<>();
    for (final JsonElement pool :
        JsonParser.parseString(Files.readString(LOOT.resolve(stage + ".json")))
            .getAsJsonObject()
            .getAsJsonArray("pools")) {
      for (final JsonElement entry : pool.getAsJsonObject().getAsJsonArray("entries")) {
        final var item = entry.getAsJsonObject();
        if (!item.has("name") || !RELIC_HOMES.containsKey(item.get("name").getAsString())) {
          continue;
        }
        final var condition = item.getAsJsonArray("conditions").get(0).getAsJsonObject();
        assertEquals("nightborn:lives_in", condition.get("condition").getAsString());
        homes.put(item.get("name").getAsString(), condition.get("structures").getAsString());
      }
    }
    assertEquals(RELIC_HOMES, homes);
  }
}
