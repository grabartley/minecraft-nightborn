package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StageNamesTest {

  static final Identifier ELDER = Identifier.of("nightborn", "elder");

  @ParameterizedTest(name = "bearer vampire {0}, viewer vampire {1}, factions shown {2} -> {3}")
  @CsvSource({
    "true, true, false, true",
    "true, false, false, false",
    "true, false, true, true",
    "true, true, true, true",
    "false, true, false, false",
    "false, false, true, false",
    "false, true, true, false"
  })
  @DisplayName("a stage is sent only to vampires, or to everyone where factions are shown")
  void shown(
      final boolean bearerVampire,
      final boolean viewerVampire,
      final boolean factionsShown,
      final boolean sent) {
    assertEquals(
        sent ? Optional.of(ELDER) : Optional.empty(),
        StageNames.shown(Optional.of(ELDER), bearerVampire, viewerVampire, factionsShown));
  }

  @ParameterizedTest(name = "viewer vampire {0}, factions shown {1}")
  @CsvSource({"true, true", "true, false", "false, true", "false, false"})
  @DisplayName("a bearer with no stage sends none")
  void noStage(final boolean viewerVampire, final boolean factionsShown) {
    assertEquals(
        Optional.empty(), StageNames.shown(Optional.empty(), true, viewerVampire, factionsShown));
  }
}
