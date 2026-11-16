package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.entity.effect.StatusEffects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SkillPowerTest {

  @Test
  @DisplayName("a power reads its blood cost, cooldown, effects and sound")
  void parses() {
    final SkillPower power =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":3,\"cooldown_ticks\":600,\"effects\":[{\"id\":\"minecraft:speed\",\"amplifier\":1,\"duration\":160}],\"sound\":\"minecraft:entity.warden.heartbeat\"}"))
            .getOrThrow();
    assertEquals(3, power.blood());
    assertEquals(600, power.cooldownTicks());
    assertEquals(StatusEffects.SPEED, power.effects().get(0).getEffectType());
    assertEquals(160, power.effects().get(0).getDuration());
    assertTrue(power.sound().isPresent());
    assertTrue(power.track().isEmpty());
    assertTrue(power.batForm().isEmpty());
    assertTrue(power.leap().isEmpty());
    assertTrue(power.mob().isEmpty());
  }

  @Test
  @DisplayName("a power can say how a vampire mob uses it")
  void mobUse() {
    final SkillPower power =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":3,\"cooldown_ticks\":600,\"mob\":{\"max_range\":20,\"tell_ticks\":12,\"tell_sound\":\"minecraft:entity.warden.sonic_charge\",\"tell_clip\":\"blood_rush_tell\"}}"))
            .getOrThrow();
    assertEquals(12, power.mob().orElseThrow().tellTicks());
  }

  @Test
  @DisplayName("a power can track the nearest vampire instead of giving effects")
  void tracks() {
    final SkillPower power =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":0,\"cooldown_ticks\":1200,\"track\":{\"range\":64,\"duration_ticks\":200}}"))
            .getOrThrow();
    assertEquals(0, power.blood());
    assertTrue(power.effects().isEmpty());
    assertEquals(new TrackPower(64, 200), power.track().orElseThrow());
  }

  @Test
  @DisplayName("a power can turn the caster into a bat or throw it into a leap")
  void shapes() {
    final SkillPower bat =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":2,\"cooldown_ticks\":100,\"bat_form\":{\"land_range\":3}}"))
            .getOrThrow();
    final SkillPower leap =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":1,\"cooldown_ticks\":100,\"leap\":{\"forward\":1.4,\"up\":0.5}}"))
            .getOrThrow();
    assertEquals(new BatFormPower(3.0F, List.of()), bat.batForm().orElseThrow());
    assertTrue(bat.leap().isEmpty());
    assertEquals(new LeapPower(1.4F, 0.5F), leap.leap().orElseThrow());
    assertTrue(leap.batForm().isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"blood\":21,\"cooldown_ticks\":0}",
        "{\"blood\":-1,\"cooldown_ticks\":0}",
        "{\"blood\":1,\"cooldown_ticks\":-5}",
        "{\"cooldown_ticks\":10}"
      })
  @DisplayName(
      "a power costing more than a full blood bar, or with a negative cooldown, is rejected")
  void rejects(final String json) {
    assertFalse(SkillPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
