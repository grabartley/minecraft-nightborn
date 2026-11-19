package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TrapDamageTest {

  static final Path TAGS = Path.of("src/main/resources/data/nightborn/tags/damage_type");

  static boolean tagged(final String tag, final RegistryKey<DamageType> key) throws IOException {
    return JsonParser.parseString(
            Files.readString(TAGS.resolve(tag + ".json"), StandardCharsets.UTF_8))
        .getAsJsonObject()
        .getAsJsonArray("values")
        .asList()
        .stream()
        .anyMatch(value -> value.getAsString().equals(key.getValue().toString()));
  }

  @ParameterizedTest(name = "{0}: burns mist {1}")
  @CsvSource({"silver, false", "holy_light, true"})
  @DisplayName(
      "trap damage ships as data, never finishes a vampire, and holy light burns mist as the sun"
          + " does")
  void ships(final String path, final boolean harmsMist) throws IOException {
    final RegistryKey<DamageType> key =
        path.equals("silver") ? TrapDamage.SILVER : TrapDamage.HOLY_LIGHT;
    assertEquals(path, key.getValue().getPath());
    assertTrue(
        Files.exists(Path.of("src/main/resources/data/nightborn/damage_type/" + path + ".json")));
    assertEquals(harmsMist, tagged("harms_mist", key));
    assertEquals(false, tagged("kills_vampires", key));
  }
}
