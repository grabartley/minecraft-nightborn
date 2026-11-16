package com.grahambartley.nightborn.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.MobSounds;
import com.grahambartley.nightborn.occult.OccultSounds;
import com.grahambartley.nightborn.vomit.VomitEffects;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class NightbornSoundsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final List<String> LANGS =
      List.of("en_us", "de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn");

  static Stream<Identifier> registered() {
    Stream.of(
            NightbornSounds.DRINK,
            MobSounds.VAMPIRE_BITE,
            OccultSounds.FAMILIAR_AMBIENT,
            VomitEffects.RETCH_MALE)
        .forEach(Objects::requireNonNull);
    return Registries.SOUND_EVENT.getIds().stream()
        .filter(id -> "nightborn".equals(id.getNamespace()));
  }

  static Stream<Arguments> events() throws IOException {
    return sounds().entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }

  static Stream<Arguments> files() throws IOException {
    return sounds().entrySet().stream()
        .flatMap(
            e ->
                e.getValue().getAsJsonObject().getAsJsonArray("sounds").asList().stream()
                    .filter(NightbornSoundsTest::isFile)
                    .map(s -> Arguments.of(e.getKey(), name(s))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("registered")
  @DisplayName("every registered sound is defined in sounds.json")
  void registeredSoundIsDefined(final Identifier id) throws IOException {
    assertTrue(sounds().has(id.getPath()), id.toString());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("events")
  @DisplayName("every subtitled sound has its subtitle in every language")
  void subtitleIsTranslated(final String event, final JsonElement definition) throws IOException {
    final JsonObject sound = definition.getAsJsonObject();
    if (!sound.has("subtitle")) {
      return;
    }
    final String key = sound.get("subtitle").getAsString();
    for (final String lang : LANGS) {
      assertTrue(lang(lang).has(key), lang + " " + key);
    }
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("files")
  @DisplayName("every sound file named in sounds.json is shipped")
  void soundFileIsShipped(final String event, final String name) {
    final String path = name.substring(name.indexOf(':') + 1);
    assertTrue(name.startsWith("nightborn:"), event + " " + name);
    assertTrue(Files.isRegularFile(ASSETS.resolve("sounds/" + path + ".ogg")), event + " " + path);
  }

  @ParameterizedTest(name = "drains={0}")
  @CsvSource({"true, entity.vampire.drain", "false, entity.vampire.drink"})
  @DisplayName("a draining sip sounds different from a plain drink")
  void sip(final boolean drains, final String path) {
    assertEquals(path, NightbornSounds.sip(drains).getId().getPath());
  }

  @ParameterizedTest(name = "open={0}")
  @CsvSource({"true, block.coffin.open", "false, block.coffin.close"})
  @DisplayName("the coffin lid has its own open and close sounds")
  void lid(final boolean open, final String path) {
    assertEquals(path, NightbornSounds.lid(open).getId().getPath());
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {0L, 1L, 42L, 9001L, -7L})
  @DisplayName("a varied pitch stays within a tenth of normal")
  void variedPitch(final long seed) {
    final Random random = Random.create(seed);
    for (int i = 0; i < 200; i++) {
      final float pitch = NightbornSounds.variedPitch(random);
      assertTrue(pitch >= 0.9F && pitch <= 1.1F, String.valueOf(pitch));
    }
  }

  private static boolean isFile(final JsonElement sound) {
    return sound.isJsonPrimitive()
        || !sound.getAsJsonObject().has("type")
        || !"event".equals(sound.getAsJsonObject().get("type").getAsString());
  }

  private static String name(final JsonElement sound) {
    return sound.isJsonPrimitive()
        ? sound.getAsString()
        : sound.getAsJsonObject().get("name").getAsString();
  }

  private static JsonObject sounds() throws IOException {
    return JsonParser.parseString(Files.readString(ASSETS.resolve("sounds.json")))
        .getAsJsonObject();
  }

  private static JsonObject lang(final String lang) throws IOException {
    return JsonParser.parseString(Files.readString(ASSETS.resolve("lang/" + lang + ".json")))
        .getAsJsonObject();
  }
}
