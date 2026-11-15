package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigFormatTest {

  @ParameterizedTest(name = "{0} problems")
  @ValueSource(ints = {0, 1, 3})
  @DisplayName("a read keeps its own fixed copy of the problems it found")
  void problemsAreCopied(final int count) {
    final List<String> problems = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      problems.add("problem " + i);
    }
    final ConfigFormat.Read<Integer> read = new ConfigFormat.Read<>(1, problems);
    problems.add("added later");
    assertEquals(count, read.problems().size());
    assertThrows(UnsupportedOperationException.class, () -> read.problems().add("more"));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(ints = {0, 7, -3, 1000})
  @DisplayName("a format built from parts gives its defaults and calls its reader and writer")
  void builtFromParts(final int value) {
    final ConfigFormat<Integer> format =
        ConfigFormat.of(
            value,
            root -> new ConfigFormat.Read<>(root.get("n").getAsInt() + 1, List.of()),
            number -> {
              final JsonObject root = new JsonObject();
              root.addProperty("n", number * 2);
              return root;
            });
    assertEquals(value, format.defaults());
    assertEquals(value * 2, format.write(value).get("n").getAsInt());
    assertEquals(value * 2 + 1, format.read(format.write(value)).value());
  }
}
