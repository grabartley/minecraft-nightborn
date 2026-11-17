package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WornLookTest {

  @ParameterizedTest(name = "{0} then {1}: resolved {2} times")
  @CsvSource({"a, a, 1", "a, b, 2", "'', '', 1", "'', a, 2"})
  void resolvesATextureOnlyWhenTheLookChanges(
      final String first, final String second, final int resolved) {
    final List<String> asked = new ArrayList<>();
    final WornLook worn = new WornLook();
    for (final String look : List.of(first, second, second)) {
      assertEquals(
          Identifier.of("nightborn", "look_" + look),
          worn.texture(
              look,
              id -> {
                asked.add(id);
                return Identifier.of("nightborn", "look_" + id);
              }));
    }
    assertEquals(resolved, asked.size());
  }
}
