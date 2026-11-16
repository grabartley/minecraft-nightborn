package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
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
    assertTrue(power.mesmerise().isEmpty());
    assertTrue(power.familiar().isEmpty());
    assertTrue(power.fog().isEmpty());
    assertTrue(power.shadowStep().isEmpty());
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

  @Test
  @DisplayName("a power can mesmerise whatever the caster stares at")
  void mesmerises() {
    final SkillPower power =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":2,\"cooldown_ticks\":300,\"mesmerise\":{\"range\":12,\"stare_ticks\":20,\"duration_ticks\":200,\"player_duration_ticks\":60,\"player_slowness\":2}}"))
            .getOrThrow();
    assertEquals(20, power.mesmerise().orElseThrow().stareTicks());
    assertTrue(power.effects().isEmpty());
  }

  @Test
  @DisplayName("a power can summon a familiar, breathe a fog or step through the shadows")
  void occult() {
    final SkillPower power =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":4,\"cooldown_ticks\":600,"
                        + "\"familiar\":{\"lifetime_ticks\":1200,\"health\":10,\"damage\":3,\"scout_range\":24,\"scout_ticks\":200,\"mark_radius\":8,\"mark_ticks\":200,\"carry_slots\":9},"
                        + "\"fog\":{\"radius\":4,\"duration_ticks\":160,\"effects\":[{\"id\":\"minecraft:blindness\",\"duration\":60}]},"
                        + "\"shadow_step\":{\"range\":16,\"behind\":1.5}}"))
            .getOrThrow();
    assertEquals(1200, power.familiar().orElseThrow().lifetimeTicks());
    assertEquals(4.0F, power.fog().orElseThrow().radius());
    assertEquals(16.0F, power.shadowStep().orElseThrow().range());
  }
}
