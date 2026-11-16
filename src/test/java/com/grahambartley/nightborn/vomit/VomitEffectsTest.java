package com.grahambartley.nightborn.vomit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VomitEffectsTest {

  @ParameterizedTest(name = "slim={0} -> {1}")
  @CsvSource({"true, entity.vampire.female.retch", "false, entity.vampire.male.retch"})
  @DisplayName("a slim skin retches with the female voice, a wide one with the male")
  void retchVoice(final boolean slim, final String path) {
    assertEquals(path, VomitEffects.retch(slim).getId().getPath());
  }
}
