package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.fight.Cutting;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BodyRuleTest {

  private static final Path RULES =
      Path.of("src/main/resources/data/nightborn/nightborn/body_rule");

  private static boolean loads(final String json) {
    return BodyRule.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess();
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"maimable\":true} | true",
        "{\"maimable\":false} | true",
        "{\"maimable\":false,\"cutting\":\"marked\"} | true",
        "{\"maimable\":true,\"only_when_staggered\":true,\"chance_scale\":0.5} | true",
        "{\"maimable\":true,\"chance_scale\":0} | true",
        "{} | false",
        "{\"maimable\":true,\"chance_scale\":1.5} | false",
        "{\"maimable\":true,\"chance_scale\":-0.1} | false",
        "{\"maimable\":true,\"cutting\":\"teeth\"} | false",
        "{\"maimable\":false,\"only_when_staggered\":true} | false",
        "{\"maimable\":false,\"chance_scale\":0.5} | false"
      })
  @DisplayName("a body rule says whether it can be maimed, and only a maimable body takes limits")
  void parses(final String json, final boolean expected) {
    assertEquals(expected, loads(json));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "vampire, true, false, 1.0, WEAPON",
    "hunter, true, false, 1.0, WEAPON",
    "guild_master, true, true, 0.5, WEAPON",
    "vampire_lord, true, true, 0.5, ALL",
    "veteran_hunter, true, true, 0.5, ALL",
    "eldest, false, false, 1.0, MARKED",
    "grand_inquisitor, false, false, 1.0, MARKED"
  })
  @DisplayName(
      "the great bosses keep their bodies, leaders are maimed only staggered and half as often")
  void shipped(
      final String id,
      final boolean maimable,
      final boolean onlyWhenStaggered,
      final float chanceScale,
      final Cutting cutting)
      throws IOException {
    assertEquals(
        new BodyRule(maimable, onlyWhenStaggered, chanceScale, cutting),
        BodyRule.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(Files.readString(RULES.resolve(id + ".json"))))
            .getOrThrow());
  }
}
