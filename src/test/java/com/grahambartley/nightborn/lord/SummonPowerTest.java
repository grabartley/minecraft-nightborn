package com.grahambartley.nightborn.lord;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SummonPowerTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"entity\":\"nightborn:vampire_bat\",\"count\":4} | true",
        "{\"entity\":\"minecraft:bat\",\"count\":{\"type\":\"minecraft:uniform\",\"min_inclusive\":3,\"max_inclusive\":5},\"spread\":2} | true",
        "{\"entity\":\"minecraft:bat\",\"count\":0} | false",
        "{\"entity\":\"minecraft:bat\",\"count\":9} | false",
        "{\"entity\":\"minecraft:bat\",\"count\":2,\"spread\":9} | false",
        "{\"entity\":\"nightborn:no_such_thing\",\"count\":2} | false",
        "{\"count\":2} | false"
      })
  @DisplayName("a summoning reads a real entity type and between one and eight of them")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, SummonPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "roll {0} with spread {1} lands {2} off")
  @CsvSource({
    "0.0, 2.0, -2.0",
    "0.5, 2.0, 0.0",
    "1.0, 2.0, 2.0",
    "0.75, 1.5, 0.75",
    "0.3, 0.0, 0.0"
  })
  @DisplayName("summoned creatures appear within the spread on each side of the caster")
  void offset(final double roll, final float spread, final double offset) {
    final SummonPower power =
        new SummonPower(
            Registries.ENTITY_TYPE.getEntry(EntityType.BAT),
            ConstantIntProvider.create(1),
            spread,
            Optional.empty(),
            Optional.empty(),
            Optional.empty());
    assertEquals(offset, power.offset(roll), 1.0E-6);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"entity\":\"minecraft:zombie\",\"count\":3,\"lifetime_ticks\":600,\"health\":20,\"damage\":4} | 600 | 20 | 4",
        "{\"entity\":\"minecraft:bat\",\"count\":3} | 0 | 0 | 0"
      })
  @DisplayName("a summon may set how long its creatures stay and how strong they are")
  void binds(final String json, final int lifetime, final float health, final float damage) {
    final SummonPower power =
        SummonPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(lifetime, power.lifetimeTicks().orElse(0));
    assertEquals(health, power.health().orElse(0.0F));
    assertEquals(damage, power.damage().orElse(0.0F));
  }
}
