package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StandGiftTest {

  private static final StandGift LAST_STAND = new StandGift(0.25F, 160, 3600, true, List.of());

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"below\":0.25,\"duration_ticks\":160,\"cooldown_ticks\":3600,\"steadfast\":true,\"attributes\":[{\"attribute\":\"minecraft:generic.attack_damage\",\"amount\":3,\"operation\":\"add_value\"}]} | 0.25 | 160 | 3600 | true | 1",
        "{\"below\":0.5,\"duration_ticks\":20,\"cooldown_ticks\":0} | 0.5 | 20 | 0 | false | 0"
      })
  @DisplayName("a stand reads when it rises, how long it lasts, its cooldown and what it gives")
  void reads(
      final String json,
      final float below,
      final int duration,
      final int cooldown,
      final boolean steadfast,
      final int attributes) {
    final StandGift gift =
        StandGift.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(below, gift.below());
    assertEquals(duration, gift.durationTicks());
    assertEquals(cooldown, gift.cooldownTicks());
    assertEquals(steadfast, gift.steadfast());
    assertEquals(attributes, gift.attributes().size());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {"{\"below\":0.25}", "{\"below\":2,\"duration_ticks\":160,\"cooldown_ticks\":0}"})
  @DisplayName("a stand out of range or missing a value is refused")
  void refuses(final String json) {
    assertTrue(StandGift.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "health {0}/{1}, cooldown {2}: rises {3}")
  @CsvSource({
    "4, 20, 0, true",
    "5, 20, 0, false",
    "4, 20, 10, false",
    "0, 20, 0, false",
    "1, 20, 0, true"
  })
  @DisplayName("a stand rises below its share of health, once its cooldown is over")
  void rises(final float health, final float max, final long cooldown, final boolean rises) {
    assertEquals(rises, LAST_STAND.rises(health, max, cooldown));
  }
}
