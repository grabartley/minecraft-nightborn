package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.gear.GearItems;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class EldestSpoilsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data/nightborn");

  static Stream<Arguments> spoils() {
    return Stream.of(
        Arguments.of("eldest_ash", EldestSpoils.ASH),
        Arguments.of("ashen_silver_sword", EldestSpoils.ASHEN_SILVER_SWORD),
        Arguments.of("ashen_silver_axe", EldestSpoils.ASHEN_SILVER_AXE),
        Arguments.of("ashen_silver_stake", EldestSpoils.ASHEN_SILVER_STAKE),
        Arguments.of("eldest_blade", EldestSpoils.BLADE),
        Arguments.of("eldest_crown", EldestSpoils.CROWN),
        Arguments.of("eldest_mantle", EldestSpoils.MANTLE),
        Arguments.of("eldest_greaves", EldestSpoils.GREAVES),
        Arguments.of("eldest_boots", EldestSpoils.BOOTS),
        Arguments.of("fang_armor_trim_smithing_template", EldestSpoils.FANG_TRIM),
        Arguments.of("sigil_armor_trim_smithing_template", EldestSpoils.SIGIL_TRIM),
        Arguments.of("music_disc_eldest", EldestSpoils.DISC),
        Arguments.of("tame_bat", EldestSpoils.TAME_BAT));
  }

  private static JsonObject json(final Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
  }

  private static double damage(final Item item) {
    return new ItemStack(item)
            .getOrDefault(
                DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT)
            .modifiers()
            .stream()
            .filter(entry -> entry.attribute().equals(EntityAttributes.GENERIC_ATTACK_DAMAGE))
            .mapToDouble(entry -> entry.modifier().value())
            .sum();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("spoils")
  @DisplayName("every reward is registered under its id and has an item model")
  void registered(final String id, final Item item) {
    assertEquals("nightborn:" + id, Registries.ITEM.getId(item).toString());
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + id + ".json")), id + " has no model");
  }

  @Test
  @DisplayName("ashen silver hits harder than silver, and the blade harder than a netherite sword")
  void hitsHarder() {
    assertTrue(damage(EldestSpoils.ASHEN_SILVER_SWORD) > damage(GearItems.SILVER_SWORD));
    assertTrue(damage(EldestSpoils.ASHEN_SILVER_AXE) > damage(GearItems.SILVER_AXE) - 1.0);
    assertTrue(damage(EldestSpoils.BLADE) > damage(Items.NETHERITE_SWORD));
    assertTrue(damage(EldestSpoils.BLADE) <= damage(Items.NETHERITE_SWORD) + 1.0);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("spoils")
  @DisplayName(
      "the blade, the armour and the tame bat are epic, and the blade and armour fireproof")
  void rarity(final String id, final Item item) {
    final boolean set = id.startsWith("eldest_") && !id.equals("eldest_ash");
    final ItemStack stack = new ItemStack(item);
    assertEquals(set || id.equals("tame_bat"), stack.getRarity() == Rarity.EPIC, id);
    assertEquals(set, stack.contains(DataComponentTypes.FIRE_RESISTANT), id);
  }

  @Test
  @DisplayName("the disc plays the Eldest's song, and one stacks to one")
  void disc() throws IOException {
    final ItemStack disc = new ItemStack(EldestSpoils.DISC);
    assertEquals(1, disc.getMaxCount());
    assertEquals(EldestSpoils.SONG, disc.get(DataComponentTypes.JUKEBOX_PLAYABLE).song().key());
    final JsonObject song = json(DATA.resolve("jukebox_song/eldest.json"));
    final String event = song.get("sound_event").getAsString();
    assertTrue(
        json(ASSETS.resolve("sounds.json")).has(Identifier.of(event).getPath()),
        event + " is not in sounds.json");
    assertEquals(event, Registries.SOUND_EVENT.getId(EldestSpoils.SONG_SOUND).toString());
  }

  @Test
  @DisplayName("the set tags name the four armour pieces and the blade")
  void tags() throws IOException {
    assertEquals(
        Set.of(
            "nightborn:eldest_crown",
            "nightborn:eldest_mantle",
            "nightborn:eldest_greaves",
            "nightborn:eldest_boots"),
        values(DATA.resolve("tags/item/eldest_set_armor.json")));
    assertEquals(
        Set.of("nightborn:eldest_blade"), values(DATA.resolve("tags/item/eldest_set_blades.json")));
    assertTrue(
        values(DATA.resolve("tags/item/silver_weapons.json"))
            .containsAll(
                Set.of(
                    "nightborn:ashen_silver_sword",
                    "nightborn:ashen_silver_axe",
                    "nightborn:ashen_silver_stake")));
    assertTrue(
        values(DATA.resolve("tags/item/stakes.json")).contains("nightborn:ashen_silver_stake"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("spoils")
  @DisplayName(
      "the Eldest's loot table can drop every reward that is not made at a smithing table, and"
          + " every smithed one has its recipe")
  void obtainable(final String id, final Item item) throws IOException {
    final Set<String> drops = new HashSet<>();
    for (final JsonElement pool :
        json(DATA.resolve("loot_table/entities/vampire/eldest.json")).getAsJsonArray("pools")) {
      for (final JsonElement entry : pool.getAsJsonObject().getAsJsonArray("entries")) {
        if (entry.getAsJsonObject().has("name")) {
          drops.add(entry.getAsJsonObject().get("name").getAsString());
        }
      }
    }
    if (id.startsWith("ashen_silver_")) {
      final JsonObject recipe = json(DATA.resolve("recipe/" + id + ".json"));
      assertEquals("minecraft:smithing_transform", recipe.get("type").getAsString());
      assertEquals(
          "nightborn:" + id.replace("ashen_", ""),
          recipe.getAsJsonObject("base").get("item").getAsString());
      assertEquals(
          "nightborn:eldest_ash", recipe.getAsJsonObject("addition").get("item").getAsString());
      assertEquals("nightborn:" + id, recipe.getAsJsonObject("result").get("id").getAsString());
    } else {
      assertTrue(drops.contains("nightborn:" + id), id + " never drops");
    }
  }

  private static Set<String> values(final Path tag) throws IOException {
    final Set<String> values = new HashSet<>();
    final JsonArray array = json(tag).getAsJsonArray("values");
    array.forEach(value -> values.add(value.getAsString()));
    return values;
  }
}
