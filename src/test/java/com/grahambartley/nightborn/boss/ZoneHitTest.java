package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ZoneHitTest {

  @ParameterizedTest(name = "damage {0}: wounds {1}")
  @CsvSource({"0, false", "0.5, true", "6, true"})
  @DisplayName("a mark that deals no damage only lays its other effects")
  void wounds(final float damage, final boolean wounds) {
    assertEquals(wounds, ZoneHit.wounds(damage));
  }

  @Test
  @DisplayName("a hit reads its damage type, effects, weakening, fire and sun from data")
  void parses() {
    final ZoneHit hit =
        ZoneHit.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"damage_type\":\"minecraft:in_fire\",\"effects\":[{\"id\":"
                        + "\"minecraft:slowness\",\"amplifier\":9,\"duration\":60}],"
                        + "\"weaken\":{\"source\":\"consecrated\",\"strength\":2,\"ticks\":40},"
                        + "\"ignite_seconds\":4,\"sun_ticks\":15}"))
            .getOrThrow();
    assertEquals(Optional.of(DamageTypes.IN_FIRE), hit.damageType());
    assertEquals(StatusEffects.SLOWNESS, hit.effects().get(0).getEffectType());
    assertEquals(9, hit.effects().get(0).getAmplifier());
    assertEquals(Optional.of(new ZoneWeaken("consecrated", 2, 40)), hit.weaken());
    assertEquals(4.0F, hit.igniteSeconds());
    assertEquals(15, hit.sunTicks());
    assertEquals(
        ZoneHit.PLAIN,
        ZoneHit.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }
}
