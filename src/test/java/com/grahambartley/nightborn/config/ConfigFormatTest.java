package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
