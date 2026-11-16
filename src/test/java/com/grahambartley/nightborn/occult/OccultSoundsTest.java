package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.sound.SoundEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class OccultSoundsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");

  static Stream<Arguments> sounds() {
    return Stream.of(
        Arguments.of(OccultSounds.FAMILIAR_AMBIENT, "entity.ghostly_familiar.ambient"),
        Arguments.of(OccultSounds.FAMILIAR_HURT, "entity.ghostly_familiar.hurt"),
        Arguments.of(OccultSounds.FAMILIAR_DEATH, "entity.ghostly_familiar.death"),
        Arguments.of(OccultSounds.FAMILIAR_FADE, "entity.ghostly_familiar.fade"));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("sounds")
  @DisplayName("each familiar sound is defined in sounds.json with a translated subtitle")
  void defined(final SoundEvent sound, final String path) throws IOException {
    assertEquals("nightborn:" + path, sound.getId().toString());
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
