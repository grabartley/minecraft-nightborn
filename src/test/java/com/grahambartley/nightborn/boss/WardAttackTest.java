package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WardAttackTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"entity\":\"nightborn:vampire_bat\",\"count\":3,\"ticks\":500} | true",
        "{\"entity\":\"minecraft:bat\",\"count\":8,\"spread\":3,\"ticks\":20} | true",
        "{\"entity\":\"nightborn:vampire\",\"count\":2,\"ticks\":600,\"shields\":false} | true",
        "{\"entity\":\"minecraft:bat\",\"count\":0,\"ticks\":500} | false",
        "{\"entity\":\"minecraft:bat\",\"count\":9,\"ticks\":500} | false",
        "{\"entity\":\"minecraft:bat\",\"count\":3,\"ticks\":10} | false",
        "{\"entity\":\"nightborn:no_such_thing\",\"count\":3,\"ticks\":500} | false",
        "{\"count\":3,\"ticks\":500} | false"
      })
  @DisplayName("a ward names a real creature, between one and eight keepers and how long it lasts")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, WardAttack.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} keepers alive, {1} ticks old: holds {2}")
  @CsvSource({"3, 0, true", "1, 499, true", "1, 500, false", "0, 10, false", "0, 0, false"})
  @DisplayName("the ward holds while a keeper lives and its time has not run out")
  void holds(final int alive, final long age, final boolean holds) {
    assertEquals(holds, WardAttack.holds(alive, age, 500));
  }

  @ParameterizedTest(name = "{0} keepers alive, {1} ticks old: spent {2}")
  @CsvSource({"3, 0, false", "1, 499, false", "1, 500, true", "2, 900, true", "0, 900, false"})
  @DisplayName("keepers still standing when their time runs out are sent away")
  void spent(final int alive, final long age, final boolean spent) {
    assertEquals(spent, WardAttack.spent(alive, age, 500));
  }

  @ParameterizedTest(name = "keeper {0} of {1}")
  @CsvSource({"0, 3", "1, 3", "2, 3", "0, 1", "5, 8"})
  @DisplayName("the keepers appear in a ring round the boss")
  void ring(final int index, final int count) {
    final WardAttack ward =
        new WardAttack(Registries.ENTITY_TYPE.getEntry(EntityType.BAT), count, 3.0F, 500, true);
    assertEquals(3.0, Math.hypot(ward.offsetX(index), ward.offsetZ(index)), 1.0E-9);
    assertEquals(
        2.0 * Math.PI * index / count,
        (Math.atan2(ward.offsetZ(index), ward.offsetX(index)) + 2.0 * Math.PI) % (2.0 * Math.PI),
        1.0E-9);
  }
}
