package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PrayerPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}, lingers {2}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"radius\":6,\"health\":6}; false; false",
        "{\"radius\":8,\"duration_ticks\":200,\"health_per_second\":1,\"shelters\":true}; false; true",
        "{\"radius\":2,\"duration_ticks\":100,\"health_per_second\":2,\"harm_per_second\":3,\"harm_type\":\"nightborn:holy_light\",\"spot_range\":16}; false; true",
        "{\"radius\":6}; true; false",
        "{\"radius\":6,\"duration_ticks\":100}; true; false",
        "{\"radius\":2,\"duration_ticks\":100,\"harm_per_second\":3}; true; false",
        "{\"radius\":0,\"health\":6}; true; false",
        "{\"health\":6}; true; false"
      })
  @DisplayName("a prayer heals at once or lasts, and one that harms names how")
  void reads(final String json, final boolean refused, final boolean lingers) {
    final var read = PrayerPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    assertEquals(refused, read.isError());
    if (!refused) {
      assertEquals(lingers, read.getOrThrow().lingers());
    }
  }
}
