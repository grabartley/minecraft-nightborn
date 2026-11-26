package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.ShippedClips;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.effect.StatusEffects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SkillPowerTest {

  private static final Path SKILLS = Path.of("src/main/resources/data/nightborn/nightborn/skill");

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
    assertTrue(power.mesmerise().isEmpty());
    assertTrue(power.familiar().isEmpty());
    assertTrue(power.fog().isEmpty());
    assertTrue(power.shadowStep().isEmpty());
    assertTrue(power.vanish().isEmpty());
    assertTrue(power.rally().isEmpty());
    assertTrue(power.summon().isEmpty());
    assertTrue(power.mob().isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"blood\":3,\"cooldown_ticks\":400,\"rally\":{\"range\":32}} | true | false",
        "{\"blood\":4,\"cooldown_ticks\":600,\"summon\":{\"entity\":\"nightborn:vampire_bat\",\"count\":4}} | false | true"
      })
  @DisplayName("a power can rally a household or summon creatures to the caster")
  void ralliesOrSummons(final String json, final boolean rally, final boolean summon) {
    final SkillPower power =
        SkillPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(rally, power.rally().isPresent());
    assertEquals(summon, power.summon().isPresent());
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

  @ParameterizedTest(name = "{0} blocks, gone {1} ticks")
  @CsvSource({"4, 5", "2, 1", "8, 20"})
  @DisplayName("a power can vanish the caster a set distance for a set time")
  void vanishes(final float distance, final int goneTicks) {
    final SkillPower power =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":1,\"cooldown_ticks\":100,\"vanish\":{\"distance\":"
                        + distance
                        + ",\"gone_ticks\":"
                        + goneTicks
                        + "}}"))
            .getOrThrow();
    assertEquals(distance, power.vanish().orElseThrow().distance());
    assertEquals(goneTicks, power.vanish().orElseThrow().goneTicks());
    assertTrue(power.shadowStep().isEmpty());
  }

  @ParameterizedTest(name = "shadow step {0}, vanish {1}: loads {2}")
  @CsvSource({"true, false, true", "false, true, true", "true, true, false", "false, false, true"})
  @DisplayName("a power moves its caster one way at most")
  void oneMove(final boolean step, final boolean vanish, final boolean loads) {
    final String json =
        "{\"blood\":1,\"cooldown_ticks\":100"
            + (step ? ",\"shadow_step\":{\"range\":16,\"behind\":1.5}" : "")
            + (vanish ? ",\"vanish\":{\"distance\":4,\"gone_ticks\":5}" : "")
            + "}";
    assertEquals(
        loads, SkillPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"blood\":2,\"cooldown_ticks\":400,\"pounce\":{\"range\":10,\"pin_ticks\":40,\"player_pin_ticks\":20}} | pounce",
        "{\"blood\":4,\"cooldown_ticks\":1200,\"mist\":{\"duration_ticks\":160,\"harm\":2}} | mist",
        "{\"blood\":2,\"cooldown_ticks\":600,\"command\":{\"range\":12,\"duration_ticks\":600}} | command",
        "{\"blood\":5,\"cooldown_ticks\":3600,\"glamour\":{\"duration_ticks\":600}} | glamour",
        "{\"blood\":1,\"cooldown_ticks\":600,\"sense\":{\"range\":24,\"duration_ticks\":160}} | sense",
        "{\"blood\":6,\"cooldown_ticks\":6000,\"swarm\":{\"radius\":12,\"duration_ticks\":300,\"damage\":1,\"every_ticks\":20,\"recent_ticks\":200},\"frees\":{\"skills\":[\"nightborn:occult/shadow_step\"],\"duration_ticks\":300}} | swarm"
      })
  @DisplayName("a power reads each new kind of effect from its own block")
  void newBlocks(final String json, final String block) {
    final SkillPower power =
        SkillPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(block.equals("pounce"), power.pounce().isPresent());
    assertEquals(block.equals("mist"), power.mist().isPresent());
    assertEquals(block.equals("command"), power.command().isPresent());
    assertEquals(block.equals("glamour"), power.glamour().isPresent());
    assertEquals(block.equals("sense"), power.sense().isPresent());
    assertEquals(block.equals("swarm"), power.swarm().isPresent() && power.frees().isPresent());
  }

  @ParameterizedTest(name = "leap {0}, mist {1}: loads {2}")
  @CsvSource({"true, false, true", "false, true, true", "true, true, false"})
  @DisplayName("a power that leaps cannot also turn its caster to mist")
  void oneShape(final boolean leap, final boolean mist, final boolean loads) {
    final String json =
        "{\"blood\":1,\"cooldown_ticks\":100"
            + (leap ? ",\"leap\":{\"forward\":1.4,\"up\":0.55}" : "")
            + (mist ? ",\"mist\":{\"duration_ticks\":160,\"harm\":2}" : "")
            + "}";
    assertEquals(
        loads, SkillPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} -> leaves the ground {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"blood\":2,\"cooldown_ticks\":400,\"leap\":{\"forward\":1.5,\"up\":0.6}} | true",
        "{\"blood\":2,\"cooldown_ticks\":400,\"pounce\":{\"range\":10,\"pin_ticks\":40,\"player_pin_ticks\":20}} | true",
        "{\"blood\":3,\"cooldown_ticks\":600,\"effects\":[{\"id\":\"minecraft:speed\",\"duration\":160}]} | false",
        "{\"blood\":0,\"cooldown_ticks\":500,\"net\":{\"range\":24,\"speed\":2,\"ground_ticks\":100,\"damage_type\":\"nightborn:silver\"}} | false"
      })
  @DisplayName("a leap or pounce leaves the ground, so a netted caster cannot use it")
  void leavesTheGround(final String json, final boolean leaves) {
    assertEquals(
        leaves,
        SkillPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .leavesTheGround());
  }

  @ParameterizedTest(name = "{0}: fights {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"blood\":0,\"cooldown_ticks\":120} | 0",
        "{\"blood\":0,\"cooldown_ticks\":240,\"riposte\":{\"guard_ticks\":10,\"reach\":2.5,\"charge_ticks\":10,\"push\":3,\"stagger_ticks\":40}} | 1",
        "{\"blood\":0,\"cooldown_ticks\":200,\"stake_throw\":{\"range\":20,\"speed\":2,\"damage\":4,\"stagger_ticks\":20,\"damage_type\":\"nightborn:thrown_stake\"}} | 1",
        "{\"blood\":0,\"cooldown_ticks\":300,\"quick_shot\":{\"speed\":3.15,\"divergence\":1}} | 1"
      })
  @DisplayName("a power may guard, throw a stake or fire a crossbow, or none of these")
  void fights(final String json, final int count) {
    assertEquals(
        count,
        SkillPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .fight()
            .count());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"blood\":0,\"cooldown_ticks\":20,\"clip\":\"power_gaze\"} | power_gaze",
        "{\"blood\":0,\"cooldown_ticks\":20} | ''"
      })
  @DisplayName("a power may name the one clip any caster plays when it is used")
  void clip(final String json, final String clip) {
    assertEquals(
        clip,
        SkillPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .clip()
            .orElse(""));
  }

  static Stream<Path> shippedPowers() throws IOException {
    try (Stream<Path> files = Files.walk(SKILLS)) {
      return files
          .filter(file -> file.toString().endsWith(".json"))
          .filter(file -> power(file).isPresent())
          .toList()
          .stream();
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shippedPowers")
  @DisplayName(
      "every shipped power plays a clip the player rig ships, unless it changes the player's"
          + " whole shape")
  void shippedPlayerClips(final Path file) {
    final SkillPower power = power(file).orElseThrow();
    final boolean changesShape =
        power.batForm().isPresent() || power.wingedForm().isPresent() || power.mist().isPresent();
    assertEquals(!changesShape, power.clip().isPresent(), "names a clip");
    power.clip().ifPresent(clip -> assertTrue(ShippedClips.vampire().contains(clip), clip));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shippedPowers")
  @DisplayName(
      "every shipped power has a sound, and its sounds are softer than full volume so repeating"
          + " them never grates")
  void shippedSoundsAreSoft(final Path file) {
    final SkillPower power = power(file).orElseThrow();
    assertTrue(power.sound().isPresent(), "has a sound");
    Stream.of(power.sound(), power.endSound(), power.trap().map(trap -> trap.spring().sound()))
        .flatMap(Optional::stream)
        .forEach(sound -> assertTrue(sound.volume() < 1.0F, sound.id() + " " + sound.volume()));
  }

  @Test
  @DisplayName("a power may name a sound it makes when it ends")
  void endSound() {
    final SkillPower power =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":0,\"cooldown_ticks\":20,\"end_sound\":{\"id\":\"minecraft:block.bell.use\",\"volume\":0.5}}"))
            .getOrThrow();
    assertEquals(0.5F, power.endSound().orElseThrow().volume());
  }

  private static Optional<SkillPower> power(final Path file) {
    try {
      final JsonObject skill = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
      return skill.has("power")
          ? Optional.of(SkillPower.CODEC.parse(JsonOps.INSTANCE, skill.get("power")).getOrThrow())
          : Optional.empty();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
