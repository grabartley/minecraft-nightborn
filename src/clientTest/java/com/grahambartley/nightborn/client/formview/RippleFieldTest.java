package com.grahambartley.nightborn.client.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RippleFieldTest {

  private static final float LIFE = 2.0F;

  private static Ripple ripple(final double born, final float strength) {
    return new Ripple(new Vec3d(born, 0.0, 0.0), born, 10.0F, strength);
  }

  @ParameterizedTest(name = "{0} sounds")
  @ValueSource(ints = {1, 5, 32, 33, 100, 1000})
  @DisplayName("a noisy place never holds more than the fixed number of live ripples")
  void neverOverCapacity(final int sounds) {
    final RippleField field = new RippleField();
    for (int i = 0; i < sounds; i++) {
      field.add(ripple(1.0, 0.5F), 1.0, LIFE);
    }
    assertEquals(Math.min(sounds, RippleField.CAPACITY), field.live().size());
  }

  @Test
  @DisplayName("when full, a stronger sound takes the place of the faintest ripple")
  void strongerReplacesFaintest() {
    final RippleField field = new RippleField();
    field.add(ripple(0.0, 0.3F), 0.0, LIFE);
    for (int i = 1; i < RippleField.CAPACITY; i++) {
      field.add(ripple(0.0, 0.9F), 0.0, LIFE);
    }
    final Ripple loud = ripple(0.0, 1.0F);
    field.add(loud, 0.0, LIFE);
    assertTrue(field.live().contains(loud));
    assertTrue(field.live().stream().noneMatch(each -> each.strength() == 0.3F));
  }

  @Test
  @DisplayName("when full, a fainter sound than every live ripple is dropped")
  void fainterDropped() {
    final RippleField field = new RippleField();
    for (int i = 0; i < RippleField.CAPACITY; i++) {
      field.add(ripple(0.0, 0.9F), 0.0, LIFE);
    }
    final Ripple faint = ripple(0.0, 0.25F);
    field.add(faint, 0.0, LIFE);
    assertFalse(field.live().contains(faint));
  }

  @ParameterizedTest(name = "at {0}s")
  @ValueSource(doubles = {2.0, 2.5, 10.0})
  @DisplayName("ripples are gone once their life is over")
  void expires(final double now) {
    final RippleField field = new RippleField();
    field.add(ripple(0.0, 1.0F), 0.0, LIFE);
    field.expire(now, LIFE);
    assertTrue(field.live().isEmpty());
  }

  @Test
  @DisplayName("clearing leaves no ripple behind")
  void clears() {
    final RippleField field = new RippleField();
    field.add(ripple(0.0, 1.0F), 0.0, LIFE);
    field.clear();
    assertTrue(field.live().isEmpty());
  }
}
