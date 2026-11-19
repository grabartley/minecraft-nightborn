package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PowerAimTest {

  @ParameterizedTest(name = "power [{0}]")
  @ValueSource(
      strings = {
        "",
        "{\"blood\":3,\"cooldown_ticks\":600,\"effects\":[{\"id\":\"minecraft:speed\",\"duration\":160}]}",
        "{\"blood\":4,\"cooldown_ticks\":600,\"fog\":{\"radius\":4,\"duration_ticks\":160,\"effects\":[]}}",
        "{\"blood\":4,\"cooldown_ticks\":1200,\"familiar\":{\"lifetime_ticks\":1200,\"health\":10,\"damage\":3,\"scout_range\":24,\"scout_ticks\":200,\"mark_radius\":8,\"mark_ticks\":200,\"carry_slots\":9}}"
      })
  @DisplayName("a missing power, or one that neither stares nor steps, aims at nothing")
  void none(final String json) {
    final Optional<SkillPower> power =
        json.isEmpty()
            ? Optional.empty()
            : Optional.of(
                SkillPower.CODEC
                    .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                    .getOrThrow());
    assertSame(PowerAim.NONE, PowerAim.of(null, power, PowerHeading.NONE));
  }

  @ParameterizedTest(name = "{0}: aims {1}, reach {2}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"blood\":2,\"cooldown_ticks\":400,\"pounce\":{\"range\":10,\"pin_ticks\":40,\"player_pin_ticks\":20}} | true | 10",
        "{\"blood\":2,\"cooldown_ticks\":600,\"hex\":{\"range\":16,\"duration_ticks\":400,\"player_duration_ticks\":200,\"healing\":0.5}} | true | 16",
        "{\"blood\":2,\"cooldown_ticks\":600,\"command\":{\"range\":12,\"duration_ticks\":600}} | true | 0",
        "{\"blood\":5,\"cooldown_ticks\":3600,\"glamour\":{\"duration_ticks\":600}} | false | 0",
        "{\"blood\":0,\"cooldown_ticks\":400,\"trap\":{\"range\":4,\"duration_ticks\":6000,\"limit\":2,\"seen_within\":6,\"stepped\":true,\"damage_type\":\"nightborn:silver\",\"look\":\"nightborn:snare\",\"spring\":{\"sound\":\"minecraft:block.iron_trapdoor.close\",\"particle\":{\"type\":\"minecraft:crit\"}}}} | true | 0",
        "{\"blood\":0,\"cooldown_ticks\":1200,\"bait\":{\"range\":4,\"duration_ticks\":1200,\"lure_range\":24,\"scent_range\":8,\"look\":\"nightborn:bait\"}} | true | 0",
        "{\"blood\":0,\"cooldown_ticks\":500,\"net\":{\"range\":24,\"speed\":2,\"ground_ticks\":100,\"damage_type\":\"nightborn:silver\"}} | false | 0"
      })
  @DisplayName("a pounce or a hex aims at one creature, a command at one held, a trap at a spot")
  void aims(final String json, final boolean aims, final float reach) {
    final SkillPower power =
        SkillPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(aims, PowerAim.aims(power));
    assertEquals(reach, PowerAim.targetRange(power).orElse(0.0F));
    assertEquals(PowerAim.targetRange(power).isPresent(), PowerAim.NONE.unseen(power));
    assertEquals(power.command().isPresent(), PowerAim.NONE.subjectless(power));
    assertEquals(power.traps().places(), PowerAim.NONE.unplaced(power));
    assertEquals(false, PowerAim.NONE.nowhere(power));
  }
}
