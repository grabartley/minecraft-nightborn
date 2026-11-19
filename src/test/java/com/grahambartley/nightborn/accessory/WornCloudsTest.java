package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WornCloudsTest {

  @ParameterizedTest(name = "{0} ticks at {1}: {2}")
  @CsvSource({"100, 0.5, 50", "101, 0.5, 51", "1, 0.5, 1", "100, 1.0, 100"})
  @DisplayName("a cloud's effect is cut by the wearer's share, never to nothing")
  void shorten(final int ticks, final double share, final int left) {
    final StatusEffectInstance shortened =
        WornClouds.shorten(new StatusEffectInstance(StatusEffects.POISON, ticks, 1), share);
    assertEquals(left, shortened.getDuration());
    assertEquals(1, shortened.getAmplifier());
  }

  @Test
  @DisplayName("an effect with no end is left as it is")
  void endless() {
    final StatusEffectInstance endless =
        new StatusEffectInstance(StatusEffects.POISON, StatusEffectInstance.INFINITE);
    assertSame(endless, WornClouds.shorten(endless, 0.5));
  }
}
