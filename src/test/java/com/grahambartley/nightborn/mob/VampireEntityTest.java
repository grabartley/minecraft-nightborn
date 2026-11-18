package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VampireEntityTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {VampireEntity.MOURN_CLIP})
  @DisplayName("a vampire mourns a housemate with a clip its rig ships")
  void mournClipShips(final String clip) {
    assertTrue(ShippedClips.vampire().contains(clip));
  }
}
