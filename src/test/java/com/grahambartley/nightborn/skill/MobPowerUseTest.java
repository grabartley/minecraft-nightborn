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
        "{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"a:b\"}"
      })
  @DisplayName("a mob use with no tell, or a range or tell out of bounds, is rejected")
  void rejects(final String json) {
    assertFalse(
        MobPowerUse.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} blocks: {1}")
  @CsvSource({"3.9, false", "4, true", "12, true", "20, true", "20.1, false"})
  @DisplayName("a power is used only inside its range")
  void inRange(final double distance, final boolean inRange) {
    assertEquals(inRange, parse(VALID).inRange(distance));
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
      "every shipped mob power plays its tell and its one clip on each rig that can roll it")
  void shippedClips(final Path file) throws IOException {
    final JsonObject power =
        JsonParser.parseString(Files.readString(file)).getAsJsonObject().getAsJsonObject("power");
    if (power != null && power.has("mob")) {
      final MobPowerUse use = parse(power.get("mob").toString());
      for (final String rig : rigs(file)) {
        final Set<String> clips = ShippedClips.of(rig);
        assertTrue(clips.contains(use.tellClip()), rig + " " + use.tellClip());
        if (power.has("clip")) {
          final String clip = power.get("clip").getAsString();
          assertTrue(clips.contains(clip), rig + " " + clip);
        }
      }
    }
  }

  private static List<String> rigs(final Path file) {
    return file.getParent().getFileName().toString().equals("lord")
        ? List.of("vampire_lord")
        : List.of("vampire", "vampire_lord");
  }
}
