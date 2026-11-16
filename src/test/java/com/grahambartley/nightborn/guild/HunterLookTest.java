package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class HunterLookTest {

  private static final Path MAIN = Path.of("src/main/resources/assets");
  private static final Path CLIENT = Path.of("src/client/resources/assets");

  @ParameterizedTest(name = "female {0}: {1}")
  @CsvSource({"true, FEMALE", "false, MALE"})
  void aHunterLooksMaleOrFemale(final boolean female, final HunterLook look) {
    assertEquals(look, HunterLook.hunter(female));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(HunterLook.class)
  void everyLookShipsItsRigAndTexture(final HunterLook look) {
    assertTrue(
        Files.exists(CLIENT.resolve(look.geo().getNamespace()).resolve(look.geo().getPath())));
    assertTrue(
        Files.exists(
            MAIN.resolve(look.texture().getNamespace()).resolve(look.texture().getPath())));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(HunterPose.class)
  void theSharedAnimationsHoldEveryPose(final HunterPose pose) throws IOException {
    final String animations =
        Files.readString(
            CLIENT
                .resolve(HunterLook.ANIMATION.getNamespace())
                .resolve(HunterLook.ANIMATION.getPath()));
    assertTrue(
        JsonParser.parseString(animations)
            .getAsJsonObject()
            .getAsJsonObject("animations")
            .has(pose.animation()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(HunterLook.class)
  void everyRigHasTheBonesTheRendererUses(final HunterLook look) throws IOException {
    final String geo =
        Files.readString(CLIENT.resolve(look.geo().getNamespace()).resolve(look.geo().getPath()));
    assertTrue(geo.contains("\"name\": \"head\"") || geo.contains("\"name\":\"head\""));
    assertTrue(geo.contains("item_right"));
  }
}
