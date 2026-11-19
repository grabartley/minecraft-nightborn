package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class WingedFormRendererTest {

  private static final Path SKILLS = Path.of("src/main/resources/data/nightborn/nightborn/skill");

  static List<Identifier> rigs() throws IOException {
    final List<Identifier> rigs = new ArrayList<>();
    try (Stream<Path> files = Files.walk(SKILLS)) {
      for (final Path file : files.filter(f -> f.toString().endsWith(".json")).toList()) {
        final JsonObject power = GeoAssets.read(file).getAsJsonObject("power");
        if (power != null
            && power.has("winged_form")
            && power.getAsJsonObject("winged_form").has("rig")) {
          rigs.add(Identifier.of(power.getAsJsonObject("winged_form").get("rig").getAsString()));
        }
      }
    }
    return rigs;
  }

  @Test
  @DisplayName("Night Terror names its own rig")
  void someFormNamesARig() throws IOException {
    assertFalse(rigs().isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("a winged rig ships its model, skin and glowing eyes")
  void shipsArt(final Identifier rig) {
    for (final Path file :
        List.of(
            GeoAssets.file(
                Identifier.of(rig.getNamespace(), "geo/entity/" + rig.getPath() + ".geo.json")),
            GeoAssets.mainFile(
                Identifier.of(rig.getNamespace(), "textures/entity/" + rig.getPath() + ".png")),
            GeoAssets.mainFile(
                Identifier.of(
                    rig.getNamespace(), "textures/entity/" + rig.getPath() + "_glowmask.png")))) {
      assertTrue(Files.exists(file), file.toString());
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("a winged rig ships every flight clip and every strike")
  void shipsClips(final Identifier rig) throws IOException {
    final JsonObject clips = GeoAssets.animations(GeoAssets.clips(rig.getPath()));
    for (final BatFormClip clip : BatFormClip.values()) {
      for (final String animation : clip.animations()) {
        assertTrue(clips.has(animation), rig + " " + animation);
      }
    }
    for (final WingedStrike strike : WingedStrike.values()) {
      assertTrue(clips.has(strike.animation()), rig + " " + strike.animation());
    }
  }
}
