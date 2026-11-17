package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
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
  @DisplayName(
      "a phase reads its colour, powers, attacks, music, body, defence, voice and flight, with"
          + " plain defaults")
  void reads() {
    final BossPhase full =
        BossPhase.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"name\":\"blood\",\"health\":35,\"color\":\"purple\",\"gap_ticks\":30,"
                        + "\"powers\":[\"nightborn:occult/grave_fog\"],"
                        + "\"attacks\":[\"nightborn:blood_pools\"],"
                        + "\"music\":\"nightborn:music.boss.eldest_final\","
                        + "\"body\":{\"attributes\":{\"minecraft:generic.armor\":16},"
                        + "\"max_hit\":10,\"blazing_eyes\":true},"
                        + "\"defence\":{\"guard_hits\":3,\"window_ticks\":80,"
                        + "\"break_damage\":30,\"break_away\":10,\"guard_knock\":1.8,"
                        + "\"retarget_chance\":50,\"counters\":[\"nightborn:blood_step\"]},"
                        + "\"voice\":{\"sound\":\"minecraft:entity.warden.angry\","
                        + "\"every_ticks\":100,\"pitch\":0.6},"
                        + "\"flight\":{\"height\":6,\"radius\":9,\"speed\":0.3,"
                        + "\"ground_ticks\":80}}"))
            .getOrThrow();
    assertEquals(
        new BossPhase(
            "blood",
            35,
            BossBar.Color.PURPLE,
            List.of(Identifier.of("nightborn", "occult/grave_fog")),
            List.of(Identifier.of("nightborn", "blood_pools")),
            30,
            Optional.of(Identifier.of("nightborn", "music.boss.eldest_final")),
            new PhaseBody(Map.of(Identifier.ofVanilla("generic.armor"), 16.0), 10.0F, true),
            new PhaseDefence(
                3, 80, 30.0F, 10.0F, 1.8F, 50, List.of(Identifier.of("nightborn", "blood_step"))),
            Optional.of(new PhaseVoice(Identifier.ofVanilla("entity.warden.angry"), 100, 0.6F)),
            Optional.of(new PhaseFlight(6.0F, 9.0F, 0.3F, 80))),
        full);
    final BossPhase plain =
        BossPhase.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"name\":\"court\",\"health\":30}"))
            .getOrThrow();
    assertEquals(
        new BossPhase(
            "court",
            30,
            BossBar.Color.RED,
            List.of(),
            List.of(),
            60,
            Optional.empty(),
            PhaseBody.PLAIN,
            PhaseDefence.NONE,
            Optional.empty(),
            Optional.empty()),
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
            Optional.empty(),
            PhaseBody.PLAIN,
            PhaseDefence.NONE,
            Optional.empty(),
            Optional.empty());
    assertEquals(casts, phase.casts(Identifier.of(power)));
    assertEquals("nightborn.boss.phase.court", phase.translationKey());
  }
}
