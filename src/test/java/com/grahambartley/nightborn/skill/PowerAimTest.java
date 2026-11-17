package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
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
}
