package com.grahambartley.nightborn.gramophone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MusicDiscsTest {

  private static final Path RESOURCES = Path.of("src/main/resources");
  private static final Path ASSETS = RESOURCES.resolve("assets/nightborn");

  static Stream<String> tracks() {
    return MusicDiscs.TRACKS.stream();
  }

  private static JsonObject json(final Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("tracks")
  @DisplayName("each disc is a rare single item that plays its own song, with a model and a sprite")
  void disc(final String track) {
    final Item disc = Registries.ITEM.get(Identifier.of("nightborn", MusicDiscs.itemId(track)));
    final JukeboxPlayableComponent playable =
        disc.getDefaultStack().get(DataComponentTypes.JUKEBOX_PLAYABLE);
    assertTrue(MusicDiscs.all().contains(disc));
    assertEquals(1, disc.getMaxCount());
    assertEquals(Rarity.RARE, disc.getDefaultStack().getRarity());
    assertNotNull(playable);
    assertEquals(MusicDiscs.song(track), playable.song().key());
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + MusicDiscs.itemId(track) + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("textures/item/" + MusicDiscs.itemId(track) + ".png")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("tracks")
  @DisplayName("each song names a registered sound that streams a recording the mod ships")
  void song(final String track) throws IOException {
    final JsonObject song =
        json(RESOURCES.resolve("data/nightborn/jukebox_song/" + track + ".json"));
    final Identifier sound = Identifier.of("nightborn", MusicDiscs.soundId(track));
    assertEquals(sound.toString(), song.get("sound_event").getAsString());
    assertTrue(Registries.SOUND_EVENT.containsId(sound));
    assertTrue(song.get("length_in_seconds").getAsFloat() > 0);
    final int comparator = song.get("comparator_output").getAsInt();
    assertTrue(comparator >= 1 && comparator <= 15);
    final JsonObject file =
        json(ASSETS.resolve("sounds.json"))
            .getAsJsonObject(MusicDiscs.soundId(track))
            .getAsJsonArray("sounds")
            .get(0)
            .getAsJsonObject();
    assertTrue(file.get("stream").getAsBoolean());
    final String name = file.get("name").getAsString();
    assertTrue(
        Files.exists(ASSETS.resolve("sounds/" + name.substring(name.indexOf(':') + 1) + ".ogg")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("tracks")
  @DisplayName("each disc and its song are named in English")
  void named(final String track) throws IOException {
    final JsonObject lang = json(ASSETS.resolve("lang/en_us.json"));
    assertTrue(lang.has("item.nightborn." + MusicDiscs.itemId(track)));
    assertTrue(lang.has("jukebox_song.nightborn." + track));
  }

  @Test
  @DisplayName("every disc gives a different comparator signal")
  void comparatorSignalsDiffer() throws IOException {
    final Set<Integer> signals = new HashSet<>();
    for (final String track : MusicDiscs.TRACKS) {
      signals.add(
          json(RESOURCES.resolve("data/nightborn/jukebox_song/" + track + ".json"))
              .get("comparator_output")
              .getAsInt());
    }
    assertEquals(MusicDiscs.TRACKS.size(), signals.size());
  }

  @Test
  @DisplayName("the disc tag that structure loot rolls names exactly the registered discs")
  void tagNamesEveryDisc() throws IOException {
    final JsonArray values =
        json(RESOURCES.resolve("data/nightborn/tags/item/music_discs.json"))
            .getAsJsonArray("values");
    final Set<String> tagged = new HashSet<>();
    values.forEach(value -> tagged.add(value.getAsString()));
    final Set<String> registered = new HashSet<>();
    MusicDiscs.all().forEach(disc -> registered.add(Registries.ITEM.getId(disc).toString()));
    assertEquals(registered, tagged);
  }

  static Stream<String> discSoundFiles() throws IOException {
    final JsonObject sounds = json(ASSETS.resolve("sounds.json"));
    return sounds.keySet().stream()
        .filter(event -> event.startsWith("music_disc."))
        .flatMap(
            event ->
                sounds.getAsJsonObject(event).getAsJsonArray("sounds").asList().stream()
                    .map(sound -> sound.getAsJsonObject().get("name").getAsString()));
  }

  @ParameterizedTest(name = "{0} is mono, so it fades with distance from what plays it")
  @MethodSource("discSoundFiles")
  void everyDiscTrackIsMono(final String sound) throws IOException {
    final byte[] ogg =
        Files.readAllBytes(
            ASSETS.resolve("sounds").resolve(Identifier.of(sound).getPath() + ".ogg"));
    final String header = new String(ogg, 0, 256, StandardCharsets.ISO_8859_1);
    final int identification = header.indexOf("\u0001vorbis");
    assertTrue(identification >= 0, sound);
    assertEquals(1, ogg[identification + 11]);
  }
}
