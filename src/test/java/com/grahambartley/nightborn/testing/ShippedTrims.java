package com.grahambartley.nightborn.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.RegistryKey;

public final class ShippedTrims {

  private static final Path ASSETS = Path.of("src/main/resources/assets");
  private static final Path DATA = Path.of("src/main/resources/data/nightborn");
  private static final Path LANG = ASSETS.resolve("nightborn/lang/en_us.json");

  private ShippedTrims() {}

  private static JsonObject json(final Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
  }

  public static void assertShipped(final String name, final RegistryKey<ArmorTrimPattern> key)
      throws IOException {
    assertEquals("nightborn:" + name, key.getValue().toString());
    final String template = "nightborn:" + name + "_armor_trim_smithing_template";
    final JsonObject pattern = json(DATA.resolve("trim_pattern/" + name + ".json"));
    assertEquals(template, pattern.get("template_item").getAsString());
    final String title = pattern.getAsJsonObject("description").get("translate").getAsString();
    assertEquals("trim_pattern.nightborn." + name, title);
    assertTrue(json(LANG).has(title));
    final JsonObject recipe =
        json(DATA.resolve("recipe/" + name + "_armor_trim_smithing_template_smithing_trim.json"));
    assertEquals("minecraft:smithing_trim", recipe.get("type").getAsString());
    assertEquals(template, recipe.getAsJsonObject("template").get("item").getAsString());
  }

  public static void assertDrawsItsOwnPattern(final String name) throws IOException {
    assertEquals(
        "nightborn:" + name,
        json(DATA.resolve("trim_pattern/" + name + ".json")).get("asset_id").getAsString());
    final String atlas =
        Files.readString(
            ASSETS.resolve("minecraft/atlases/armor_trims.json"), StandardCharsets.UTF_8);
    for (final String suffix : new String[] {"", "_leggings"}) {
      assertTrue(
          Files.exists(
              ASSETS.resolve("nightborn/textures/trims/models/armor/" + name + suffix + ".png")));
      assertTrue(atlas.contains("\"nightborn:trims/models/armor/" + name + suffix + "\""));
    }
  }
}
