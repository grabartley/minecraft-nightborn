package com.grahambartley.nightborn.client.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.formview.FormViewEcho;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SoundRippleTest {

  private static final FormViewEcho ECHO =
      new FormViewEcho(5.0F, 0.3F, 24.0F, 2.0F, 16.0F, 40.0F, 1.5F, 1.0F, 0.1F, 0.6F, 30, 0.6F);

  @ParameterizedTest(name = "volume {0} reaches {1} at strength {2}")
  @CsvSource({"0.1, 1.6, 0.25", "0.5, 8.0, 0.5", "1.0, 16.0, 1.0", "4.0, 40.0, 1.0"})
  @DisplayName("a louder sound makes a stronger ripple that travels further")
  void louderIsStronger(final float volume, final float range, final float strength) {
    final Ripple ripple =
        SoundRipple.of(ECHO, new Vec3d(1, 2, 3), volume, Vec3d.ZERO, 7.0).orElseThrow();
    assertEquals(range, ripple.range(), 1.0E-5F);
    assertEquals(strength, ripple.strength(), 1.0E-5F);
    assertEquals(new Vec3d(1, 2, 3), ripple.origin());
    assertEquals(7.0, ripple.bornSeconds());
  }

  @ParameterizedTest(name = "{0} blocks away: {1}")
  @CsvSource({"10, true", "55, true", "56.5, false", "200, false"})
  @DisplayName("a sound too far off for its ripple to come near sends none")
  void farIgnored(final double distance, final boolean heard) {
    assertEquals(
        heard, SoundRipple.of(ECHO, new Vec3d(distance, 0, 0), 1.0F, Vec3d.ZERO, 0.0).isPresent());
  }

  @Test
  @DisplayName("a silent sound sends no ripple")
  void silentIgnored() {
    assertTrue(SoundRipple.of(ECHO, Vec3d.ZERO, 0.0F, Vec3d.ZERO, 0.0).isEmpty());
  }
}
