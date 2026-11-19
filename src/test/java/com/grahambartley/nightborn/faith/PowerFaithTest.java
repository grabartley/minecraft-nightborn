package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PowerFaithTest {

  @ParameterizedTest(name = "{0}: any {1}, arms {2}, spot {3}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | false | false | 0",
        "{\"prayer\":{\"radius\":6,\"health\":6}} | true | false | 0",
        "{\"prayer\":{\"radius\":2,\"duration_ticks\":100,\"health_per_second\":2,\"spot_range\":16}} | true | false | 16",
        "{\"rebuke\":{\"range\":8,\"arc_degrees\":90,\"push\":1.5,\"silence_ticks\":40}} | true | false | 0",
        "{\"blessing\":{\"range\":8,\"duration_ticks\":1200}} | true | true | 0",
        "{\"blood_fire\":{\"health_cost\":4,\"duration_ticks\":1800,\"damage\":3,\"burn_ticks\":100}} | true | true | 0"
      })
  @DisplayName("a power may pray, rebuke, bless or light a weapon, and says what it needs")
  void reads(final String json, final boolean any, final boolean arms, final float spot) {
    final PowerFaith faith =
        PowerFaith.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(any, faith.any());
    assertEquals(arms, faith.armsAWeapon());
    assertEquals(spot, faith.spotRange().orElse(0.0F));
    assertEquals(!any, faith.equals(PowerFaith.NONE));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"rebuke\":{\"range\":8,\"arc_degrees\":90,\"push\":1.5,\"silence_ticks\":40},\"prayer\":{\"radius\":6,\"health\":6}}",
        "{\"blessing\":{\"range\":8,\"duration_ticks\":1200},\"blood_fire\":{\"health_cost\":4,\"duration_ticks\":1800,\"damage\":3,\"burn_ticks\":100}}"
      })
  @DisplayName("a power does one act of faith only")
  void oneOnly(final String json) {
    assertEquals(
        true,
        PowerFaith.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
