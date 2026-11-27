package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
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
import org.junit.jupiter.params.provider.CsvSource;

class GearTagsTest {

  private static final Path DATA = Path.of("src/main/resources/data");

  private static Stream<String> items(final String tag) {
    final int split = tag.indexOf(':');
    final Path file =
        DATA.resolve(tag.substring(0, split))
            .resolve("tags/item")
            .resolve(tag.substring(split + 1) + ".json");
    try {
      return JsonParser.parseString(Files.readString(file))
          .getAsJsonObject()
          .getAsJsonArray("values")
          .asList()
          .stream()
          .map(JsonElement::getAsString)
          .flatMap(value -> value.startsWith("#") ? items(value.substring(1)) : Stream.of(value));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  @DisplayName("the reverse grip tag holds every stake, so a new stake thrusts with no code")
  void everyStake() {
    assertEquals("nightborn:reverse_grip", GearTags.REVERSE_GRIP.id().toString());
    assertEquals(items("nightborn:stakes").toList(), items("nightborn:reverse_grip").toList());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"nightborn:wooden_stake", "nightborn:silver_stake", "nightborn:ashen_silver_stake"})
  @DisplayName("each stake holds the reverse grip")
  void stake(final String item) {
    assertEquals(List.of(item), items("nightborn:reverse_grip").filter(item::equals).toList());
  }
}
