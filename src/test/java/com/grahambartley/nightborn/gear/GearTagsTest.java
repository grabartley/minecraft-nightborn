package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class GearTagsTest {

  private static final Path TAGS = Path.of("src/main/resources/data/nightborn/tags/item");
  private static final Path MODELS = Path.of("src/main/resources/assets/nightborn/models/item");
  private static final String NAMESPACE = "nightborn:";

  private static JsonObject read(final Path file) {
    try {
      return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Stream<String> items(final String tag) {
    return read(TAGS.resolve(tag + ".json")).getAsJsonArray("values").asList().stream()
        .map(JsonElement::getAsString)
        .flatMap(
            value ->
                value.startsWith("#" + NAMESPACE)
                    ? items(value.substring(NAMESPACE.length() + 1))
                    : Stream.of(value.substring(NAMESPACE.length())));
  }

  static Stream<String> reverseGrip() {
    return items("reverse_grip");
  }

  @Test
  @DisplayName("the reverse grip tag is the data that picks the thrust, and every stake is in it")
  void everyStake() {
    assertEquals("nightborn:reverse_grip", GearTags.REVERSE_GRIP.id().toString());
    assertEquals(items("stakes").toList(), reverseGrip().toList());
    assertTrue(reverseGrip().toList().containsAll(List.of("wooden_stake", "silver_stake")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("reverseGrip")
  @DisplayName(
      "a reverse grip item is held point down in each hand and keeps vanilla's GUI, ground and"
          + " frame views")
  void heldPointDown(final String item) {
    final JsonObject model = read(MODELS.resolve(item + ".json"));
    assertEquals("minecraft:item/handheld", model.get("parent").getAsString());
    final JsonObject display = model.getAsJsonObject("display");
    for (final String hand :
        List.of(
            "thirdperson_righthand",
            "thirdperson_lefthand",
            "firstperson_righthand",
            "firstperson_lefthand")) {
      final double turn =
          display.getAsJsonObject(hand).getAsJsonArray("rotation").get(2).getAsDouble();
      assertTrue(Math.abs(turn) > 90.0, item + " " + hand + " turns the point down");
    }
    for (final String view : List.of("gui", "ground", "fixed", "head")) {
      assertFalse(display.has(view), item + " keeps vanilla's " + view + " view");
    }
  }
}
