package com.grahambartley.nightborn.enchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.loot.condition.LootCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TimeOfDayConditionTest {

  @ParameterizedTest(name = "{0} with day {1} and night {2}: {3}")
  @CsvSource({
    "DAY, true, false, true",
    "DAY, false, true, false",
    "DAY, false, false, false",
    "NIGHT, false, true, true",
    "NIGHT, true, false, false",
    "NIGHT, false, false, false"
  })
  @DisplayName("a time holds only when the world is in it, and dusk is neither")
  void holds(
      final TimeOfDayCondition.Time time,
      final boolean day,
      final boolean night,
      final boolean holds) {
    assertEquals(holds, time.holds(day, night));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"day, DAY", "night, NIGHT"})
  @DisplayName("data names the time by its id")
  void reads(final String id, final TimeOfDayCondition.Time time) {
    assertEquals(
        new TimeOfDayCondition(time),
        LootCondition.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"condition\":\"nightborn:time_of_day\",\"time\":\"" + id + "\"}"))
            .getOrThrow());
  }
}
