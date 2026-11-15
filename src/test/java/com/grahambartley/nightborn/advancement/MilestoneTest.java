package com.grahambartley.nightborn.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MilestoneTest {

  @ParameterizedTest(name = "{0} is written as {1}")
  @CsvSource({
    "BITTEN, bitten",
    "TURNED, turned",
    "FED, fed",
    "FED_WATCHED, fed_watched",
    "SUN_SURVIVED, sun_survived",
    "BAT_FORM, bat_form",
    "GARLIC_STRINGS, garlic_strings",
    "DAY_SKIPPED, day_skipped",
    "BLOODLINES, bloodlines",
    "FAMILIAR_HIRED, familiar_hired",
    "VAMPIRE_STAKED, vampire_staked",
    "HOLY_WATER_HIT, holy_water_hit",
    "SICKNESS_CURED, sickness_cured",
    "STRUCTURE_ENTERED, structure_entered"
  })
  void writesItsLowerCaseName(final Milestone milestone, final String id) {
    assertEquals(id, milestone.asString());
  }

  @Test
  void everyMilestoneHasADistinctId() {
    assertEquals(
        Milestone.values().length,
        Arrays.stream(Milestone.values())
            .map(Milestone::asString)
            .collect(Collectors.toSet())
            .size());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Milestone.class)
  void roundTripsThroughTheCodec(final Milestone milestone) {
    final var json = Milestone.CODEC.encodeStart(JsonOps.INSTANCE, milestone).getOrThrow();
    assertEquals(milestone.asString(), json.getAsString());
    assertEquals(milestone, Milestone.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
  }
}
