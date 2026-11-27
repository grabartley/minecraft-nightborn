package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SanctuaryEffectTest {

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({
    "id, nightborn:sanctuary",
    "name, effect.nightborn.sanctuary",
    "kind, BENEFICIAL",
    "colour, f2e3a6"
  })
  @DisplayName("sanctuary is a helpful pale gold effect with its own name and icon")
  void registered(final String what, final String expected) {
    final String actual =
        switch (what) {
          case "id" -> SanctuaryEffect.ENTRY.getIdAsString();
          case "name" -> SanctuaryEffect.ENTRY.value().getTranslationKey();
          case "kind" -> SanctuaryEffect.ENTRY.value().getCategory().name();
          default -> Integer.toHexString(SanctuaryEffect.ENTRY.value().getColor());
        };
    assertEquals(expected, actual);
    assertTrue(
        Files.exists(
            Path.of("src/main/resources/assets/nightborn/textures/mob_effect/sanctuary.png")));
  }
}
