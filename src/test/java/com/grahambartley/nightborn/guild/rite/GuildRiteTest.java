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
import java.util.List;
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

  @ParameterizedTest(name = "the {0} sounds")
  @CsvSource({"cast", "leave", "arrive"})
  @DisplayName(
      "each step has layered sounds the game has, the first on its first tick, all in time")
  void sounds(final String step) throws IOException {
    final GuildRite rite = shipped();
    final List<GuildRite.Cue> cues =
        switch (step) {
          case "cast" -> rite.sounds().cast();
          case "leave" -> rite.sounds().leave();
          default -> rite.sounds().arrive();
        };
    final int ticks =
        switch (step) {
          case "cast" -> rite.timing().castTicks();
          case "leave" -> rite.timing().leaveTicks();
          default -> (int) (clipSeconds("vampire", rite.clips().arrive()) * TICKS_PER_SECOND);
        };
    assertTrue(cues.size() >= 3, step + " is one bare sound");
    assertEquals(1, GuildRite.Cue.due(cues, 0).size());
    for (final GuildRite.Cue cue : cues) {
      assertTrue(Registries.SOUND_EVENT.containsId(cue.sound()), cue.sound() + " is not a sound");
      assertTrue(cue.delay() < ticks, cue.sound() + " plays after the " + step + " is over");
    }
  }

  @ParameterizedTest(name = "at tick {0}, {1} due")
  @CsvSource({"0, 2", "1, 0", "7, 1", "8, 0"})
  @DisplayName("a sound plays on the tick its delay names and no other")
  void due(final int tick, final int count) {
    final Identifier bell = Identifier.ofVanilla("block.bell.use");
    final List<GuildRite.Cue> cues =
        List.of(
            new GuildRite.Cue(bell, 1.0F, 1.0F, 0),
            new GuildRite.Cue(bell, 0.5F, 1.5F, 0),
            new GuildRite.Cue(bell, 1.0F, 2.0F, 7));
    assertEquals(count, GuildRite.Cue.due(cues, tick).size());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"light, 1.25", "break, 2.0", "search, 3.0", "rise, 2.0", "household, 3.0"})
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
