package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StakeModelAssetsTest {

  private static final Path MODELS = Path.of("src/main/resources/assets/nightborn/models/item");
  private static final String GRIP = "reverse_grip_handheld";

  private static JsonObject read(final Path file) {
    try {
      return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static JsonObject vanillaHandheld() {
    try (InputStream in =
        StakeModelAssetsTest.class.getResourceAsStream(
            "/assets/minecraft/models/item/handheld.json")) {
      assertNotNull(in);
      return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static JsonObject view(final JsonObject model, final String hand) {
    return model.getAsJsonObject("display").getAsJsonObject(hand);
  }

  private static double at(final JsonObject view, final String field, final int axis) {
    return view.getAsJsonArray(field).get(axis).getAsDouble();
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"wooden_stake", "silver_stake", "ashen_silver_stake"})
  @DisplayName("every stake takes its grip from the one reverse grip model and only sets its art")
  void stakesShareTheGrip(final String stake) {
    final JsonObject model = read(MODELS.resolve(stake + ".json"));
    assertEquals("nightborn:item/" + GRIP, model.get("parent").getAsString());
    assertEquals(Set.of("parent", "textures"), model.keySet());
  }

  @Test
  @DisplayName(
      "the reverse grip builds on vanilla's handheld model and only sets the four hand views, so"
          + " the GUI, ground, head and frame views stay vanilla's")
  void onlyTheHands() {
    final JsonObject grip = read(MODELS.resolve(GRIP + ".json"));
    assertEquals("minecraft:item/handheld", grip.get("parent").getAsString());
    assertEquals(
        Set.of(
            "thirdperson_righthand",
            "thirdperson_lefthand",
            "firstperson_righthand",
            "firstperson_lefthand"),
        grip.getAsJsonObject("display").keySet());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "thirdperson_righthand, 155, 0, -4.2, -6.5",
    "thirdperson_lefthand, -155, 0, -4.2, -6.5",
    "firstperson_righthand, -153, 1.13, -1.2, -1.71",
    "firstperson_lefthand, 153, 1.13, -1.2, -1.71"
  })
  @DisplayName(
      "each hand keeps vanilla's scale and side turn, turns the point down and back, and slides"
          + " the stake along its length so the butt sits in the fist")
  void handView(
      final String hand, final double turn, final double x, final double y, final double z) {
    final JsonObject grip = view(read(MODELS.resolve(GRIP + ".json")), hand);
    final JsonObject vanilla = view(vanillaHandheld(), hand);
    final JsonArray scale = grip.getAsJsonArray("scale");
    assertEquals(vanilla.getAsJsonArray("scale"), scale);
    assertEquals(at(vanilla, "rotation", 0), at(grip, "rotation", 0));
    assertEquals(at(vanilla, "rotation", 1), at(grip, "rotation", 1));
    assertEquals(turn, at(grip, "rotation", 2));
    assertEquals(x, at(grip, "translation", 0));
    assertEquals(y, at(grip, "translation", 1));
    assertEquals(z, at(grip, "translation", 2));
  }
}
