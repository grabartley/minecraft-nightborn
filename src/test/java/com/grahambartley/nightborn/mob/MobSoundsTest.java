package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
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
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MobSoundsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");

  static Stream<Arguments> voices() {
    return Arrays.stream(VampireRig.values())
        .flatMap(rig -> Arrays.stream(VampireVoice.values()).map(v -> Arguments.of(rig, v)));
  }

  static Stream<Arguments> hunterVoices() {
    return Arrays.stream(VampireRig.values())
        .flatMap(rig -> MobSounds.HUNTER_VOICES.stream().map(v -> Arguments.of(rig, v)));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("hunterVoices")
  @DisplayName("hunters speak with their own male and female voices, defined in sounds.json")
  void hunterVoiceIsDefined(final VampireRig rig, final VampireVoice voice) throws IOException {
    final String path = MobSounds.voicePath(MobSounds.HUNTER, rig, voice);
    assertEquals("nightborn:" + path, MobSounds.hunter(rig, voice).getId().toString());
    final JsonObject sounds =
        JsonParser.parseString(Files.readString(ASSETS.resolve("sounds.json"))).getAsJsonObject();
    assertTrue(sounds.has(path), path);
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("voices")
  @DisplayName("each rig has its own voice, defined in sounds.json with a translated subtitle")
  void voiceIsDefined(final VampireRig rig, final VampireVoice voice) throws IOException {
    final String path = MobSounds.voicePath(rig, voice);
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
