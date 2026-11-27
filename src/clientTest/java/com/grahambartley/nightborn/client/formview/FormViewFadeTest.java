package com.grahambartley.nightborn.client.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.formview.FormView;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FormViewFadeTest {

  private static FormView view(final String form, final int in, final int out) {
    return new FormView(
        Identifier.of("nightborn", form),
        0,
        in,
        out,
        1.0F,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());
  }

  private static final FormView BAT = view("bat", 10, 5);
  private static final FormView MIST = view("mist", 2, 2);

  private static FormViewFade run(
      final FormViewFade from, final Optional<FormView> to, final int ticks) {
    FormViewFade fade = from;
    for (int i = 0; i < ticks; i++) {
      fade = fade.step(to);
    }
    return fade;
  }

  @ParameterizedTest(name = "after {0} ticks: {1}")
  @CsvSource({"1, 0.1", "5, 0.5", "10, 1.0", "30, 1.0"})
  @DisplayName("taking a form fades its view in over its fade-in time, never popping")
  void fadesIn(final int ticks, final float strength) {
    assertEquals(strength, run(FormViewFade.NONE, Optional.of(BAT), ticks).strength(), 1.0E-5F);
  }

  @ParameterizedTest(name = "{0} ticks after the form ends: {1}")
  @CsvSource({"1, 0.8", "4, 0.2", "5, 0.0", "20, 0.0"})
  @DisplayName("ending the form fades its view out over its fade-out time")
  void fadesOut(final int ticks, final float strength) {
    final FormViewFade full = run(FormViewFade.NONE, Optional.of(BAT), 10);
    assertEquals(strength, run(full, Optional.empty(), ticks).strength(), 1.0E-5F);
  }

  @Test
  @DisplayName("once faded out the view leaves no trace")
  void noTrace() {
    final FormViewFade full = run(FormViewFade.NONE, Optional.of(BAT), 10);
    final FormViewFade gone = run(full, Optional.empty(), 6);
    assertEquals(FormViewFade.NONE, gone);
    assertFalse(gone.visible());
  }

  @Test
  @DisplayName("changing straight to another form fades the old view out before the new comes in")
  void switchesThroughNothing() {
    final FormViewFade full = run(FormViewFade.NONE, Optional.of(BAT), 10);
    final FormViewFade leaving = run(full, Optional.of(MIST), 4);
    assertEquals(Optional.of(BAT), leaving.shown());
    final FormViewFade arriving = run(leaving, Optional.of(MIST), 2);
    assertEquals(Optional.of(MIST), arriving.shown());
    assertEquals(0.5F, arriving.strength(), 1.0E-5F);
  }

  @ParameterizedTest(name = "part {0}: {1}")
  @CsvSource({"0.0, 0.1", "0.5, 0.15", "1.0, 0.2"})
  @DisplayName("strength is smoothed between ticks")
  void smoothsBetweenTicks(final float tickDelta, final float strength) {
    final FormViewFade fade = run(FormViewFade.NONE, Optional.of(BAT), 2);
    assertEquals(strength, fade.strength(tickDelta), 1.0E-5F);
  }

  @Test
  @DisplayName("with no form there is nothing to draw")
  void noneIsInvisible() {
    assertFalse(FormViewFade.NONE.step(Optional.empty()).visible());
    assertTrue(FormViewFade.NONE.step(Optional.of(BAT)).visible());
  }
}
