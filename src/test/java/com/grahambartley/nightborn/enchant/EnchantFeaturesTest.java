package com.grahambartley.nightborn.enchant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class EnchantFeaturesTest {

  private static final Path DATA = Path.of("src/main/resources/data");

  @Test
  @DisplayName("registering brings in the conditions, values and Bloodletting effect data uses")
  void registers() {
    for (final String condition : Set.of("vampire", "has_blood", "time_of_day")) {
      assertTrue(Registries.LOOT_CONDITION_TYPE.containsId(Identifier.of("nightborn", condition)));
    }
    for (final String value : Set.of("stake_damage", "coating_hits", "finisher_health")) {
      assertTrue(
          Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.containsId(
              Identifier.of("nightborn", value)));
    }
    assertTrue(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE.containsId(BloodlettingEffect.ID));
  }

  @ParameterizedTest(name = "{0}: max {1}, curse {2}")
  @CsvSource({
    "shroud, 3, false",
    "mistcloak, 2, false",
    "warding, 3, false",
    "thrift, 3, false",
    "bloodletting, 3, false",
    "exsanguinate, 3, false",
    "nightfall, 3, false",
    "consecration, 3, false",
    "heartseeker, 3, false",
    "daybreak, 3, false",
    "thirst, 1, true",
    "sunmarked, 1, true",
    "beacon, 1, true"
  })
  @DisplayName("curses are only found as loot and the rest come as any table enchantment does")
  void foundWhereTheyShouldBe(final String name, final int max, final boolean curse)
      throws IOException {
    final JsonObject enchantment = read("nightborn/enchantment/" + name + ".json");
    final String id = "nightborn:" + name;
    assertEquals(max, enchantment.get("max_level").getAsInt());
    assertEquals(!curse, values("minecraft/tags/enchantment/non_treasure.json").contains(id));
    assertEquals(curse, values("minecraft/tags/enchantment/curse.json").contains(id));
    assertEquals(curse, values("minecraft/tags/enchantment/treasure.json").contains(id));
    assertEquals(curse, values("minecraft/tags/enchantment/on_random_loot.json").contains(id));
  }

  @ParameterizedTest(name = "{0} needs a vampire as {1}")
  @CsvSource({"bloodletting, attacker", "nightfall, attacker", "daybreak, this"})
  @DisplayName("vampire-hand enchantments check the wielder, vampire-bane ones the target")
  void checksTheRightSide(final String name, final String entity) throws IOException {
    assertTrue(
        Files.readString(DATA.resolve("nightborn/enchantment/" + name + ".json"))
            .replaceAll("\\s", "")
            .contains("\"condition\":\"nightborn:vampire\",\"entity\":\"" + entity + "\""));
  }

  private static JsonObject read(final String path) throws IOException {
    return JsonParser.parseString(Files.readString(DATA.resolve(path))).getAsJsonObject();
  }

  private static Set<String> values(final String path) throws IOException {
    final Set<String> values = new HashSet<>();
    for (final JsonElement value : read(path).getAsJsonArray("values")) {
      values.add(value.getAsString());
    }
    return values;
  }
}
