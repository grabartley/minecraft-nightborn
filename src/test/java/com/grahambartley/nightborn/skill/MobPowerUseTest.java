package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.ShippedClips;
import com.grahambartley.nightborn.mob.brain.Consideration;
import com.grahambartley.nightborn.mob.brain.Curve;
import com.grahambartley.nightborn.mob.brain.Intent;
import com.grahambartley.nightborn.mob.brain.MobUse;
import com.grahambartley.nightborn.mob.brain.Situation;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MobPowerUseTest {

  private static final String VALID =
      "{\"min_range\":4,\"max_range\":20,\"tell_ticks\":12,\"tell_sound\":\"minecraft:entity.warden.sonic_charge\",\"tell_clip\":\"blood_rush_tell\"}";

  private static MobPowerUse parse(final String json) {
    return MobPowerUse.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
  }

  @Test
  @DisplayName("a mob use reads its range and tell")
  void parses() {
    assertEquals(
        new MobPowerUse(
            4.0F,
            20.0F,
            12,
            Identifier.of("minecraft", "entity.warden.sonic_charge"),
            "blood_rush_tell",
            false),
        parse(VALID));
  }

  @Test
  @DisplayName("the lowest range is optional")
  void optionalParts() {
    final MobPowerUse use =
        parse(
            "{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"minecraft:block.bell.use\",\"tell_clip\":\"stare\"}");
    assertEquals(0.0F, use.minRange());
    assertFalse(use.dodge());
    assertEquals(0, use.holdTicks());
  }

  @ParameterizedTest(name = "hold {0}")
  @ValueSource(ints = {0, 60, 1200})
  @DisplayName("a mob use reads the shortest time it holds a form it took")
  void hold(final int ticks) {
    assertEquals(
        ticks,
        parse(
                "{\"max_range\":32,\"tell_ticks\":1,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\",\"hold_ticks\":"
                    + ticks
                    + "}")
            .holdTicks());
  }

  @ParameterizedTest(name = "dodge {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a mob use says whether the power is cast as a dodge")
  void dodge(final boolean dodge) {
    assertEquals(
        dodge,
        parse(
                "{\"max_range\":32,\"tell_ticks\":1,\"tell_sound\":\"minecraft:block.bell.use\",\"tell_clip\":\"dodge\",\"dodge\":"
                    + dodge
                    + "}")
            .dodge());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"max_range\":65,\"tell_ticks\":5,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\"}",
        "{\"max_range\":8,\"tell_ticks\":0,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\"}",
        "{\"max_range\":8,\"tell_ticks\":101,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\"}",
        "{\"max_range\":8,\"tell_ticks\":5,\"tell_clip\":\"x\"}",
        "{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"a:b\"}",
        "{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\",\"hold_ticks\":-1}",
        "{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\",\"hold_ticks\":1201}"
      })
  @DisplayName("a mob use with no tell, or a range, tell or hold out of bounds, is rejected")
  void rejects(final String json) {
    assertFalse(
        MobPowerUse.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} blocks, intent {1}: in range {2}")
  @CsvSource({
    "3.9, FIGHT, false",
    "4, FIGHT, true",
    "20, FIGHT, true",
    "20.1, FIGHT, false",
    "12, CLOSE_IN, true",
    "12, RETREAT, false",
    "12, TRAVEL, false"
  })
  @DisplayName(
      "a power with no uses of its own is used in a fight or while closing in, inside its range")
  void defaultUses(final double distance, final Intent intent, final boolean used) {
    final Situation situation =
        new Situation(
            intent, Situation.Self.of(1.0F, 20), new Situation.Target(distance, true, 1.0F));
    assertEquals(
        used, parse(VALID).effectiveUses().stream().anyMatch(use -> use.score(situation) > 0.0));
  }

  @Test
  @DisplayName("a power that lists its own uses keeps them in place of the range default")
  void declaredUses() {
    final MobPowerUse use =
        parse(
            "{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\","
                + "\"uses\":[{\"intent\":\"retreat\",\"aim\":\"self\",\"weight\":2,"
                + "\"considerations\":[{\"fact\":\"own_health\",\"curve\":\"falling\","
                + "\"low\":0.2,\"high\":0.6}]}]}");
    assertEquals(
        List.of(
            new MobUse(
                "retreat",
                "self",
                List.of(new Consideration("own_health", Curve.FALLING, 0.2F, 0.6F)),
                2.0F)),
        use.uses());
  }

  static Stream<Path> shippedSkills() throws IOException {
    try (Stream<Path> files =
        Files.walk(Path.of("src/main/resources/data/nightborn/nightborn/skill"))) {
      return files.filter(f -> f.toString().endsWith(".json")).toList().stream();
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shippedSkills")
  @DisplayName(
      "every shipped mob power plays its tell on each rig that can roll it, and a vampire power its"
          + " one clip as well, while a hunter casts under its tell")
  void shippedClips(final Path file) throws IOException {
    final JsonObject skill = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    final JsonObject power = skill.getAsJsonObject("power");
    if (power != null && power.has("mob")) {
      final MobPowerUse use = parse(power.get("mob").toString());
      final boolean hunter = tree(skill) == SkillTree.HUNTER;
      for (final String rig : rigs(file, hunter)) {
        final Set<String> clips = ShippedClips.of(rig);
        assertTrue(clips.contains(use.tellClip()), rig + " " + use.tellClip());
        if (!hunter && power.has("clip")) {
          final String clip = power.get("clip").getAsString();
          assertTrue(clips.contains(clip), rig + " " + clip);
        }
      }
    }
  }

  private static SkillTree tree(final JsonObject skill) throws IOException {
    final Identifier branch = Identifier.of(skill.get("branch").getAsString());
    return SkillBranch.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(
                Files.readString(
                    Path.of("src/main/resources/data/nightborn/nightborn/skill_branch")
                        .resolve(branch.getPath() + ".json"))))
        .getOrThrow()
        .tree();
  }

  private static List<String> rigs(final Path file, final boolean hunter) {
    if (hunter) {
      return List.of("hunter");
    }
    return file.getParent().getFileName().toString().equals("lord")
        ? List.of("vampire_lord")
        : List.of("vampire", "vampire_lord");
  }
}
