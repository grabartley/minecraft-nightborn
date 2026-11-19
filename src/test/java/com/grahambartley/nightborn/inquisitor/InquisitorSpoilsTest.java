package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
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
class InquisitorSpoilsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data/nightborn");

  static Stream<Arguments> spoils() {
    return Stream.of(
        Arguments.of("inquisitor_mail", InquisitorSpoils.MAIL, false),
        Arguments.of("inquisitor_greatsword", InquisitorSpoils.GREATSWORD, true),
        Arguments.of("inquisitor_hat", InquisitorSpoils.HAT, true),
        Arguments.of("inquisitor_coat", InquisitorSpoils.COAT, true),
        Arguments.of("inquisitor_greaves", InquisitorSpoils.GREAVES, true),
        Arguments.of("inquisitor_boots", InquisitorSpoils.BOOTS, true),
        Arguments.of("cross_armor_trim_smithing_template", InquisitorSpoils.CROSS_TRIM, false),
        Arguments.of("chain_armor_trim_smithing_template", InquisitorSpoils.CHAIN_TRIM, false),
        Arguments.of("music_disc_inquisitor", InquisitorSpoils.DISC, false),
        Arguments.of("tame_hawk", InquisitorSpoils.TAME_HAWK, false),
        Arguments.of("broken_crucifix", InquisitorSpoils.BROKEN_CRUCIFIX_ITEM, false));
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

  private static Set<String> values(final Path tag) throws IOException {
    final Set<String> values = new HashSet<>();
    final JsonArray array = json(tag).getAsJsonArray("values");
    array.forEach(
        value ->
            values.add(
                value.isJsonObject()
                    ? value.getAsJsonObject().get("id").getAsString()
                    : value.getAsString()));
    return values;
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("spoils")
  @DisplayName("every reward is registered under its id and has an item model")
  void registered(final String id, final Item item, final boolean set) {
    assertEquals("nightborn:" + id, Registries.ITEM.getId(item).toString());
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + id + ".json")), id + " has no model");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("spoils")
  @DisplayName("the greatsword, the armour and the hawk are epic, and the set is fireproof")
  void rarity(final String id, final Item item, final boolean set) {
    final ItemStack stack = new ItemStack(item);
    assertEquals(set || id.equals("tame_hawk"), stack.getRarity() == Rarity.EPIC, id);
    assertEquals(set, stack.contains(DataComponentTypes.FIRE_RESISTANT), id);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("spoils")
  @DisplayName("the Grand Inquisitor's loot table can drop every reward, and no other table can")
  void dropsOnlyFromHim(final String id, final Item item, final boolean set) throws IOException {
    final String name = "nightborn:" + id;
    assertTrue(drops(DATA.resolve("loot_table/entities/grand_inquisitor.json")).contains(name));
    try (Stream<Path> tables = Files.walk(DATA.resolve("loot_table"))) {
      tables
          .filter(path -> path.toString().endsWith(".json"))
          .filter(path -> !path.endsWith("grand_inquisitor.json"))
          .filter(path -> !path.endsWith(id + ".json"))
          .forEach(
              path -> {
                try {
                  assertTrue(!drops(path).contains(name), name + " also drops from " + path);
                } catch (final IOException e) {
                  throw new IllegalStateException(e);
                }
              });
    }
  }

  @Test
  @DisplayName("every kill gives his mail, and the treasure pools always roll")
  void mailEveryKill() throws IOException {
    boolean mail = false;
    for (final JsonElement pool :
        json(DATA.resolve("loot_table/entities/grand_inquisitor.json")).getAsJsonArray("pools")) {
      final JsonArray entries = pool.getAsJsonObject().getAsJsonArray("entries");
      if (entries.size() == 1
          && !pool.getAsJsonObject().has("conditions")
          && "nightborn:inquisitor_mail"
              .equals(entries.get(0).getAsJsonObject().get("name").getAsString())) {
        mail = true;
      }
    }
    assertTrue(mail);
  }

  @Test
  @DisplayName("the greatsword hits harder than a netherite sword, but no harder than its axe")
  void hitsHarder() {
    assertTrue(damage(InquisitorSpoils.GREATSWORD) > damage(Items.NETHERITE_SWORD));
    assertTrue(damage(InquisitorSpoils.GREATSWORD) <= damage(Items.NETHERITE_AXE));
  }

  @Test
  @DisplayName("the disc plays the music of his last phase, and one stacks to one")
  void disc() throws IOException {
    final ItemStack disc = new ItemStack(InquisitorSpoils.DISC);
    assertEquals(1, disc.getMaxCount());
    assertEquals(InquisitorSpoils.SONG, disc.get(DataComponentTypes.JUKEBOX_PLAYABLE).song().key());
    final JsonObject song = json(DATA.resolve("jukebox_song/inquisitor.json"));
    final String event = song.get("sound_event").getAsString();
    final JsonObject sounds = json(ASSETS.resolve("sounds.json"));
    assertEquals(event, Registries.SOUND_EVENT.getId(InquisitorSpoils.SONG_SOUND).toString());
    assertEquals(
        sounds
            .getAsJsonObject("music.boss.inquisitor_final")
            .getAsJsonArray("sounds")
            .get(0)
            .getAsJsonObject()
            .get("name")
            .getAsString()
            .replace("music/", "records/"),
        sounds
            .getAsJsonObject(Identifier.of(event).getPath())
            .getAsJsonArray("sounds")
            .get(0)
            .getAsJsonObject()
            .get("name")
            .getAsString());
  }

  @Test
  @DisplayName("the set tags name the four armour pieces and the greatsword")
  void tags() throws IOException {
    assertEquals(
        Set.of(
            "nightborn:inquisitor_hat",
            "nightborn:inquisitor_coat",
            "nightborn:inquisitor_greaves",
            "nightborn:inquisitor_boots"),
        values(DATA.resolve("tags/item/inquisitor_set_armor.json")));
    assertEquals(
        Set.of("nightborn:inquisitor_greatsword"),
        values(DATA.resolve("tags/item/inquisitor_set_blades.json")));
  }

  @Test
  @DisplayName("the broken crucifix is in no tag that wards off vampires")
  void crucifixWardsNoOne() throws IOException {
    try (Stream<Path> tags = Files.walk(Path.of("src/main/resources/data"))) {
      tags.filter(path -> path.toString().contains("/tags/"))
          .filter(path -> path.toString().endsWith(".json"))
          .filter(path -> !path.toString().contains("mineable"))
          .forEach(
              path -> {
                try {
                  assertTrue(
                      !values(path).contains("nightborn:broken_crucifix"),
                      "the broken crucifix is tagged in " + path);
                } catch (final IOException e) {
                  throw new IllegalStateException(e);
                }
              });
    }
  }

  private static Set<String> drops(final Path table) throws IOException {
    final Set<String> drops = new HashSet<>();
    final JsonObject root = json(table);
    if (!root.has("pools")) {
      return drops;
    }
    for (final JsonElement pool : root.getAsJsonArray("pools")) {
      for (final JsonElement entry : pool.getAsJsonObject().getAsJsonArray("entries")) {
        if (entry.getAsJsonObject().has("name")) {
          drops.add(entry.getAsJsonObject().get("name").getAsString());
        }
      }
    }
    return drops;
  }
}
