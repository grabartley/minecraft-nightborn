package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.guild.HunterVoice;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MobSoundsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");

  static Stream<Arguments> voices() {
    return Arrays.stream(VampireRig.values())
        .flatMap(rig -> Arrays.stream(VampireVoice.values()).map(v -> Arguments.of(rig, v)));
  }

  static Stream<Arguments> hunterVoices() {
    return VampireRig.COMMON.stream()
        .flatMap(rig -> Arrays.stream(HunterVoice.values()).map(v -> Arguments.of(rig, v)));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("hunterVoices")
  @DisplayName("hunters speak as villagers, male and female, with a translated subtitle")
  void hunterVoiceIsDefined(final VampireRig rig, final HunterVoice voice) throws IOException {
    final String path = MobSounds.voicePath(MobSounds.HUNTER, rig, voice.id());
    assertEquals("nightborn:" + path, MobSounds.hunter(rig, voice).getId().toString());
    final JsonObject sounds =
        JsonParser.parseString(Files.readString(ASSETS.resolve("sounds.json"))).getAsJsonObject();
    assertTrue(sounds.has(path), path);
    final JsonObject event = sounds.getAsJsonObject(path);
    final JsonObject lang =
        JsonParser.parseString(Files.readString(ASSETS.resolve("lang/en_us.json")))
            .getAsJsonObject();
    assertTrue(lang.has(event.get("subtitle").getAsString()), path);
    event
        .getAsJsonArray("sounds")
        .forEach(
            sound ->
                assertTrue(
                    sound
                        .getAsJsonObject()
                        .get("name")
                        .getAsString()
                        .startsWith("minecraft:mob/villager/"),
                    path));
  }

  @ParameterizedTest(name = "{0} {1}")
  @CsvSource({"LORD, AMBIENT", "LORD, HURT", "ELDEST_MALE, CHEER"})
  @DisplayName("asking for a hunter voice on a rig hunters do not have fails loudly")
  void hunterHasNoSuchVoice(final VampireRig rig, final HunterVoice voice) {
    assertThrows(IllegalArgumentException.class, () -> MobSounds.hunter(rig, voice));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("voices")
  @DisplayName("each rig has its own voice, defined in sounds.json with a translated subtitle")
  void voiceIsDefined(final VampireRig rig, final VampireVoice voice) throws IOException {
    final String path = MobSounds.voicePath(MobSounds.VAMPIRE, rig, voice);
    assertEquals("nightborn:" + path, MobSounds.vampire(rig, voice).getId().toString());
    final JsonObject sounds =
        JsonParser.parseString(Files.readString(ASSETS.resolve("sounds.json"))).getAsJsonObject();
    assertTrue(sounds.has(path), path);
    final String subtitle = sounds.getAsJsonObject(path).get("subtitle").getAsString();
    final JsonObject lang =
        JsonParser.parseString(Files.readString(ASSETS.resolve("lang/en_us.json")))
            .getAsJsonObject();
    assertTrue(lang.has(subtitle), subtitle);
  }
}
