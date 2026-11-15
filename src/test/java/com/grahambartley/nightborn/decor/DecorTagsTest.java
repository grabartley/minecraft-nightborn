package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DecorTagsTest {

  private static final Path TAG =
      Path.of("src/main/resources/data/nightborn/tags/item/weapon_rack_holds.json");

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "#minecraft:swords",
        "#minecraft:axes",
        "minecraft:bow",
        "minecraft:crossbow",
        "minecraft:trident",
        "minecraft:mace",
        "#nightborn:stakes"
      })
  void theWeaponRackHoldsEveryKindOfWeapon(final String entry) throws IOException {
    assertEquals("nightborn:weapon_rack_holds", DecorTags.WEAPON_RACK_HOLDS.id().toString());
    assertTrue(
        JsonParser.parseString(Files.readString(TAG))
            .getAsJsonObject()
            .getAsJsonArray("values")
            .asList()
            .stream()
            .anyMatch(value -> value.getAsString().equals(entry)));
  }
}
