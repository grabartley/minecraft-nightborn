package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BossAttackTest {

  private static final String COMMON =
      "\"tell_ticks\":20,\"tell_clip\":\"hiss\",\"tell_sound\":\"minecraft:entity.bat.takeoff\","
          + "\"clip\":\"attack\",\"sound\":\"minecraft:entity.bat.takeoff\"";
  private static final String POWER = "\"power\":\"nightborn:lord/rally\"";
  private static final String ZONE = "\"zone\":{\"radius\":3,\"damage\":12,\"linger_ticks\":160}";
  private static final String FLURRY = "\"flurry\":{\"hits\":3,\"every_ticks\":8,\"damage\":7}";
  private static final String DRAIN =
      "\"drain\":{\"ticks\":120,\"damage\":2,\"heal\":6,\"break_damage\":18}";

  private static final String GAZE =
      "\"gaze\":{\"range\":14,\"stare_ticks\":30,\"duration_ticks\":100,"
          + "\"player_duration_ticks\":80,\"player_slowness\":6}";
  private static final String WARD =
      "\"ward\":{\"entity\":\"minecraft:bat\",\"count\":3,\"ticks\":500}";
  private static final String CHANNEL =
      "\"channel\":{\"ticks\":160,\"heal\":3,\"break_damage\":24}";

  private static String kind(final String name) {
    return switch (name) {
      case "power" -> POWER;
      case "zone" -> ZONE;
      case "flurry" -> FLURRY;
      case "drain" -> DRAIN;
      case "gaze" -> GAZE;
      case "ward" -> WARD;
      case "channel" -> CHANNEL;
      default -> throw new IllegalArgumentException(name);
    };
  }

  private static String json(final String kinds, final String extra) {
    final StringBuilder json = new StringBuilder("{").append(COMMON);
    for (final String name : kinds.isBlank() ? new String[0] : kinds.split(" ")) {
      json.append(',').append(kind(name));
    }
    return json.append(extra).append('}').toString();
  }

  @ParameterizedTest(name = "kinds [{0}] with {1}: {2}")
  @CsvSource(
      delimiter = '|',
      value = {
        "power | '' | true",
        "zone | '' | true",
        "flurry | '' | true",
        "drain | '' | true",
        "gaze | '' | true",
        "ward | '' | true",
        "channel | '' | true",
        "channel zone | '' | false",
        "gaze ward | '' | false",
        "'' | '' | false",
        "power zone | '' | false",
        "flurry drain | '' | false",
        "zone | ,\"min_range\":4,\"max_range\":16 | true",
        "zone | ,\"min_range\":16,\"max_range\":4 | false",
        "zone | ,\"weight\":0 | false",
        "zone | ,\"cooldown_ticks\":-1 | false"
      })
  @DisplayName("an attack is exactly one kind, with its ranges in order")
  void parses(final String kinds, final String extra, final boolean loads) {
    assertEquals(
        loads,
        BossAttack.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json(kinds, extra)))
            .isSuccess());
  }

  @ParameterizedTest(name = "a {0} strikes for {1} ticks")
  @CsvSource({
    "power, 0",
    "zone, 0",
    "flurry, 24",
    "drain, 120",
    "gaze, 0",
    "ward, 0",
    "channel, 160"
  })
  @DisplayName(
      "a flurry, a drain and a channel hold the boss for their length, a power and a zone do not")
  void strikeTicks(final String kind, final int ticks) {
    final BossAttack attack =
        BossAttack.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json(kind, "")))
            .getOrThrow();
    assertEquals(ticks, attack.strikeTicks());
  }

  @ParameterizedTest(name = "{0} blocks away: {1}")
  @CsvSource({"3.9, false", "4.0, true", "10.0, true", "16.0, true", "16.1, false"})
  @DisplayName("an attack is used only inside its range")
  void reaches(final double distance, final boolean reaches) {
    final BossAttack attack =
        BossAttack.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(json("zone", ",\"min_range\":4,\"max_range\":16")))
            .getOrThrow();
    assertEquals(reaches, attack.reaches(distance));
  }
}
