package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GuildRiteTest {

  private static final double TICKS_PER_SECOND = 20.0;
  private static final Path DATA =
      Path.of("src/main/resources/data/nightborn/nightborn/guild_rite/sending.json");
  private static final Path ASSETS = Path.of("src/client/resources/assets/nightborn");
  private static final Path MAIN_ASSETS = Path.of("src/main/resources/assets/nightborn");

  private static JsonObject json(final Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
  }

  private static GuildRite shipped() throws IOException {
    return GuildRite.CODEC.parse(JsonOps.INSTANCE, json(DATA)).getOrThrow();
  }

  private static double clipSeconds(final String rig, final String clip) throws IOException {
    final JsonObject clips =
        json(ASSETS.resolve("animations/entity/" + rig + ".animation.json"))
            .getAsJsonObject("animations");
    assertTrue(clips.has(clip), rig + " rig has no clip " + clip);
    return clips.getAsJsonObject(clip).get("animation_length").getAsDouble();
  }

  @ParameterizedTest(name = "the {0} clip is on the {1} rig")
  @CsvSource({"cast, hunter", "leave, vampire", "arrive, vampire"})
  @DisplayName("each clip the rite names is a clip its rig ships, and the timing fits inside it")
  void clips(final String step, final String rig) throws IOException {
    final GuildRite rite = shipped();
    final GuildRite.Timing timing = rite.timing();
    final String clip =
        switch (step) {
          case "cast" -> rite.clips().cast();
          case "leave" -> rite.clips().leave();
          default -> rite.clips().arrive();
        };
    final int ticks =
        switch (step) {
          case "cast" -> timing.castTicks();
          case "leave" -> timing.leaveTicks();
          default -> timing.settleTicks();
        };
    assertTrue(ticks <= clipSeconds(rig, clip) * TICKS_PER_SECOND, step + " outlasts its clip");
  }

  @ParameterizedTest(name = "the {0} sound")
  @CsvSource({"cast", "leave", "arrive"})
  @DisplayName("each sound the rite names is a sound the game has")
  void sounds(final String step) throws IOException {
    final GuildRite.Sounds sounds = shipped().sounds();
    final Identifier sound =
        switch (step) {
          case "cast" -> sounds.cast();
          case "leave" -> sounds.leave();
          default -> sounds.arrive();
        };
    assertTrue(Registries.SOUND_EVENT.containsId(sound), sound + " is not a sound");
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"light, 1.25", "break, 2.0", "search, 3.0", "rise, 2.0", "household, 5.0"})
  @DisplayName("the shipped rite keeps its light, its reach and a look that has a texture")
  void reach(final String what, final double expected) throws IOException {
    final GuildRite rite = shipped();
    final double actual =
        switch (what) {
          case "light" -> rite.light().radius();
          case "break" -> rite.reach().breakDistance();
          case "search" -> rite.reach().searchRadius();
          case "rise" -> rite.reach().searchRise();
          default -> rite.reach().householdReach();
        };
    assertEquals(expected, actual);
    final Identifier look = rite.light().look();
    assertTrue(
        Files.exists(MAIN_ASSETS.resolve("textures/entity/boss/mark/" + look.getPath() + ".png")));
  }

  @ParameterizedTest(name = "{0} = {1}")
  @CsvSource({
    "timing.cast_ticks, 0",
    "timing.leave_ticks, 0",
    "timing.wait_ticks, -1",
    "light.radius, 0",
    "reach.search_radius, 99",
    "reach.break_distance, 0"
  })
  @DisplayName("a rite with a value out of range does not load")
  void refuses(final String field, final String value) throws IOException {
    final JsonObject json = json(DATA);
    final String[] path = field.split("\\.");
    json.getAsJsonObject(path[0]).add(path[1], JsonParser.parseString(value));
    assertTrue(GuildRite.CODEC.parse(JsonOps.INSTANCE, json).isError(), field);
  }
}
