package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TrapGiftTest {

  @ParameterizedTest(name = "{0} covers {1}: {2}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"arm_cut\":0.25}; trapper/snare; true",
        "{\"arm_cut\":0.25}; trapper/flash_trap; true",
        "{\"power\":\"nightborn:trapper/snare\",\"extra_limit\":1}; trapper/snare; true",
        "{\"power\":\"nightborn:trapper/snare\",\"extra_limit\":1}; trapper/flash_trap; false"
      })
  @DisplayName("a gift naming a power covers only that power, and one naming none covers all")
  void covers(final String json, final String power, final boolean covered) {
    assertEquals(
        covered,
        TrapGift.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .covers(Identifier.of("nightborn", power)));
  }

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{}; false",
        "{\"senses\":true}; false",
        "{\"pinned_damage\":0.25}; false",
        "{\"arm_cut\":1.5}; true",
        "{\"extra_limit\":-1}; true",
        "{\"extra_damage\":21}; true"
      })
  @DisplayName("a gift's values stay in bounds")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, TrapGift.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
