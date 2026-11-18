package com.grahambartley.nightborn.nemesis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NemesisKindsTest {

  static NemesisKind kind(final String entity) {
    return new NemesisKind(
        Identifier.of(entity),
        Identifier.of("nightborn", "lock_of_hair"),
        Identifier.of("nightborn", "nemesis_epithets"),
        0.5F,
        false,
        0.0F,
        0,
        5,
        2,
        30,
        32.0F,
        2,
        NemesisLines.NONE);
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "nightborn:vampire, nightborn:vampire",
    "nightborn:hunter, nightborn:hunter",
    "nightborn:eldest, ''",
    "nightborn:grand_inquisitor, ''"
  })
  @DisplayName("only the mobs that data names can become nemeses")
  void finds(final String entity, final String kind) {
    final Map<Identifier, NemesisKind> kinds = new LinkedHashMap<>();
    kinds.put(Identifier.of("nightborn", "vampire"), kind("nightborn:vampire"));
    kinds.put(Identifier.of("nightborn", "hunter"), kind("nightborn:hunter"));
    assertEquals(
        kind.isEmpty() ? Optional.empty() : Optional.of(Identifier.of(kind)),
        NemesisKinds.find(kinds, Identifier.of(entity)).map(NemesisKinds.Found::id));
  }
}
