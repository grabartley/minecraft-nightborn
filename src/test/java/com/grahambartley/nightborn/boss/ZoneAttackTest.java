package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ZoneAttackTest {

  private static ZoneAttack zone(final String json) {
    return ZoneAttack.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"radius\":3,\"damage\":12} | true",
        "{\"shape\":\"line\",\"radius\":2,\"length\":18,\"anchor\":\"every_foe\",\"motion\":\"leap\",\"lift\":3.5,\"damage\":9,\"knock\":0.8,\"linger_ticks\":160,\"linger_every\":10} | true",
        "{\"damage\":12} | false",
        "{\"radius\":3} | false",
        "{\"radius\":0.2,\"damage\":12} | false",
        "{\"radius\":3,\"damage\":12,\"shape\":\"square\"} | false",
        "{\"radius\":3,\"damage\":12,\"motion\":\"fly\"} | false",
        "{\"radius\":3,\"damage\":12,\"linger_every\":0} | false"
      })
  @DisplayName("a zone needs a radius and a damage, and only knows its own shapes and motions")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, ZoneAttack.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} lands a {1} at ({2}, {3})")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"radius\":3,\"damage\":1} | circle | 10.0 | 20.0",
        "{\"radius\":3,\"damage\":1,\"anchor\":\"every_foe\"} | circle | 10.0 | 20.0",
        "{\"radius\":3,\"damage\":1,\"anchor\":\"self\"} | circle | 1.0 | 2.0",
        "{\"shape\":\"line\",\"radius\":1,\"length\":8,\"damage\":1} | line | 1.0 | 2.0"
      })
  @DisplayName("a circle sits on its anchor and a line starts at the boss")
  void lands(final String json, final String shape, final double x, final double z) {
    final ZoneShape placed = zone(json).at(1.0, 2.0, 10.0, 20.0);
    assertEquals(shape.equals("line"), placed.line());
    assertEquals(x, placed.x());
    assertEquals(z, placed.z());
  }

  @ParameterizedTest(name = "lingering {0} ticks, every {1}, at tick {2}: lingers {3}, hurts {4}")
  @CsvSource({
    "160, 10, 0, true, false",
    "160, 10, 10, true, true",
    "160, 10, 15, true, false",
    "160, 10, 160, true, true",
    "0, 10, 10, false, true"
  })
  @DisplayName("a lingering zone hurts on its beat, never on the tick it bursts")
  void lingers(
      final int ticks,
      final int every,
      final int tick,
      final boolean lingers,
      final boolean hurts) {
    final ZoneAttack zone =
        zone(
            "{\"radius\":2,\"damage\":4,\"linger_ticks\":"
                + ticks
                + ",\"linger_every\":"
                + every
                + "}");
    assertEquals(lingers, zone.lingers());
    assertEquals(hurts, zone.hurtsAt(tick));
  }
}
