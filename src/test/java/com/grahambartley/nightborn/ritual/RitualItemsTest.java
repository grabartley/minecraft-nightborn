package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Rarity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RitualItemsTest {

  private static final Path RESOURCES = Path.of("src/main/resources");

  static Stream<Item> relics() {
    return RitualItems.ALL.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("relics")
  @DisplayName("each relic is a rare single item with a model and a sprite")
  void relic(final Item relic) {
    final String path = Registries.ITEM.getId(relic).getPath();
    assertEquals("nightborn", Registries.ITEM.getId(relic).getNamespace());
    assertEquals(1, relic.getMaxCount());
    assertEquals(Rarity.RARE, relic.getDefaultStack().getRarity());
    assertTrue(Files.exists(RESOURCES.resolve("assets/nightborn/models/item/" + path + ".json")));
    assertTrue(Files.exists(RESOURCES.resolve("assets/nightborn/textures/item/" + path + ".png")));
  }

  @Test
  @DisplayName("the relic tag names exactly the registered relics")
  void tagNamesEveryRelic() throws IOException {
    final JsonArray values =
        JsonParser.parseString(
                Files.readString(RESOURCES.resolve("data/nightborn/tags/item/relics.json")))
            .getAsJsonObject()
            .getAsJsonArray("values");
    final Set<String> tagged = new HashSet<>();
    values.forEach(value -> tagged.add(value.getAsString()));
    final Set<String> registered = new HashSet<>();
    RitualItems.ALL.forEach(relic -> registered.add(Registries.ITEM.getId(relic).toString()));
    assertEquals(registered, tagged);
    assertEquals(4, tagged.size());
  }
}
