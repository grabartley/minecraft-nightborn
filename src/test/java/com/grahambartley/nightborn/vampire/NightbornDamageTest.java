package com.grahambartley.nightborn.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class NightbornDamageTest {

  static Stream<RegistryKey<DamageType>> types() {
    return Stream.of(
        NightbornDamage.BITE,
        NightbornDamage.SUN,
        NightbornDamage.STAKE,
        NightbornDamage.VOMIT,
        NightbornDamage.LANCET,
        NightbornDamage.BLEEDING,
        NightbornDamage.SEARING,
        NightbornDamage.CLEAVE);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("types")
  @DisplayName("every damage type is this mod's and ships its data file")
  void shipsItsData(final RegistryKey<DamageType> type) {
    assertEquals("nightborn", type.getValue().getNamespace());
    assertTrue(
        Files.exists(
            Path.of(
                "src/main/resources/data/nightborn/damage_type/"
                    + type.getValue().getPath()
                    + ".json")));
  }

  @ParameterizedTest(name = "{0} holds {1}")
  @CsvSource({
    "weapon_blows, minecraft:player_attack",
    "weapon_blows, minecraft:mob_attack",
    "weapon_blows, nightborn:cleave",
    "severing, nightborn:cleave"
  })
  @DisplayName("a cleave is a weapon blow that is marked to sever")
  void tagged(final String tag, final String type) throws IOException {
    assertTrue(
        JsonParser.parseString(
                Files.readString(
                    Path.of("src/main/resources/data/nightborn/tags/damage_type/" + tag + ".json")))
            .getAsJsonObject()
            .getAsJsonArray("values")
            .contains(new JsonPrimitive(type)));
  }
}
