package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigFormatTest {

  private static final ConfigFormat<Integer> NUMBER =
      new ConfigFormat<>(
          7,
          root -> new ConfigFormat.Read<>(root.get("n").getAsInt(), List.of()),
          value -> {
            final JsonObject root = new JsonObject();
            root.addProperty("n", value);
            return root;
          });

  @ParameterizedTest(name = "{0}")
  @ValueSource(ints = {0, 7, -3, 1000})
  @DisplayName("a value written by a format reads back unchanged")
  void roundTrips(final int value) {
    assertEquals(value, NUMBER.read(NUMBER.write(value)).value());
    assertEquals(7, NUMBER.defaults());
  }

  @ParameterizedTest(name = "{0} problems")
  @ValueSource(ints = {0, 1, 3})
  @DisplayName("a read keeps its own copy of the problems it found")
  void problemsAreCopied(final int count) {
    final List<String> problems = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      problems.add("problem " + i);
    }
    final ConfigFormat.Read<Integer> read = new ConfigFormat.Read<>(1, problems);
    problems.add("added later");
    assertEquals(count, read.problems().size());
  }
}
