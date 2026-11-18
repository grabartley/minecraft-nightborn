package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.effect.StatusEffectCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RestedEffectTest {

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({"id, nightborn:rested", "name, effect.nightborn.rested", "kind, BENEFICIAL"})
  @DisplayName("rested is a helpful effect shown under its own name")
  void registered(final String what, final String expected) {
    final String actual =
        switch (what) {
          case "id" -> RestedEffect.ENTRY.getIdAsString();
          case "name" -> RestedEffect.ENTRY.value().getTranslationKey();
          default -> RestedEffect.ENTRY.value().getCategory().name();
        };
    assertEquals(expected, actual);
    assertEquals(StatusEffectCategory.BENEFICIAL, RestedEffect.ENTRY.value().getCategory());
  }
}
