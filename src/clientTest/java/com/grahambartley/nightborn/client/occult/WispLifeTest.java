package com.grahambartley.nightborn.client.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.client.render.LightmapTextureManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WispLifeTest {

  @ParameterizedTest(name = "age {0} of {1}: alpha {2}")
  @CsvSource({
    "0, 20, 0.0",
    "2, 20, 0.425",
    "4, 20, 0.85",
    "12, 20, 0.425",
    "20, 20, 0.0",
    "30, 20, 0.0",
    "-2, 20, 0.0",
    "0, 0, 0.0"
  })
  @DisplayName("a wisp fades in quickly, then thins away over the rest of its life")
  void alpha(final float age, final int maxAge, final float expected) {
    assertEquals(expected, WispLife.alpha(age, maxAge), 1.0e-4F);
  }

  @ParameterizedTest(name = "age {0} of {1}: scale {2}")
  @CsvSource({"0, 20, 1.0", "10, 20, 0.675", "20, 20, 0.35", "40, 20, 0.35"})
  @DisplayName("a wisp shrinks as it rises")
  void scale(final float age, final int maxAge, final float expected) {
    assertEquals(expected, WispLife.scale(age, maxAge), 1.0e-4F);
  }

  @ParameterizedTest(name = "block {0}, sky {1}: block {2}, sky {3}")
  @CsvSource({"0, 0, 9, 0", "4, 15, 9, 15", "9, 3, 9, 3", "14, 0, 14, 0", "15, 15, 15, 15"})
  @DisplayName(
      "a wisp keeps a faint cold light in the dark and takes the room's light when that is brighter")
  void brightness(final int block, final int sky, final int expectedBlock, final int expectedSky) {
    final int packed = WispLife.brightness(LightmapTextureManager.pack(block, sky));
    assertEquals(expectedBlock, LightmapTextureManager.getBlockLightCoordinates(packed));
    assertEquals(expectedSky, LightmapTextureManager.getSkyLightCoordinates(packed));
  }
}
