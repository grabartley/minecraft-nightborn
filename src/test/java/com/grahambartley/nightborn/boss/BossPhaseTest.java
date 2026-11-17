package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossPhaseTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"name\":\"court\",\"health\":30} | true",
        "{\"name\":\"court\",\"health\":30,\"color\":\"purple\",\"gap_ticks\":40,\"powers\":[\"nightborn:core/vanish\"],\"attacks\":[\"nightborn:court_flurry\"],\"music\":\"nightborn:music.boss.eldest\"} | true",
        "{\"name\":\"court\"} | false",
        "{\"health\":30} | false",
        "{\"name\":\"court\",\"health\":0} | false",
        "{\"name\":\"court\",\"health\":30,\"gap_ticks\":0} | false"
      })
  @DisplayName("a phase needs a name and a share of health")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, BossPhase.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @Test
  @DisplayName("a phase reads its colour, powers, attacks and music, with plain defaults")
  void reads() {
    final BossPhase full =
        BossPhase.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"name\":\"blood\",\"health\":35,\"color\":\"purple\",\"gap_ticks\":30,"
                        + "\"powers\":[\"nightborn:occult/grave_fog\"],"
                        + "\"attacks\":[\"nightborn:blood_pools\"],"
                        + "\"music\":\"nightborn:music.boss.eldest_final\"}"))
            .getOrThrow();
    assertEquals(
        new BossPhase(
            "blood",
            35,
            BossBar.Color.PURPLE,
            List.of(Identifier.of("nightborn", "occult/grave_fog")),
            List.of(Identifier.of("nightborn", "blood_pools")),
            30,
            Optional.of(Identifier.of("nightborn", "music.boss.eldest_final"))),
        full);
    final BossPhase plain =
        BossPhase.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"name\":\"court\",\"health\":30}"))
            .getOrThrow();
    assertEquals(
        new BossPhase("court", 30, BossBar.Color.RED, List.of(), List.of(), 60, Optional.empty()),
        plain);
  }

  @ParameterizedTest(name = "{0} allowed: {1}")
  @CsvSource({"nightborn:core/vanish, true", "nightborn:beast/long_leap, false"})
  @DisplayName("a phase casts only the powers it lists, under its own title")
  void casts(final String power, final boolean casts) {
    final BossPhase phase =
        new BossPhase(
            "court",
            30,
            BossBar.Color.RED,
            List.of(Identifier.of("nightborn", "core/vanish")),
            List.of(),
            60,
            Optional.empty());
    assertEquals(casts, phase.casts(Identifier.of(power)));
    assertEquals("nightborn.boss.phase.court", phase.translationKey());
  }
}
