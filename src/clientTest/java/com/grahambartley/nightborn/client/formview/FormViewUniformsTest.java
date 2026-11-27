package com.grahambartley.nightborn.client.formview;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.formview.FormView;
import com.grahambartley.nightborn.formview.FormViewEcho;
import com.grahambartley.nightborn.formview.FormViewTint;
import com.grahambartley.nightborn.formview.FormViewTremble;
import com.grahambartley.nightborn.formview.FormViewWash;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class FormViewUniformsTest {

  private static final FormViewEcho ECHO =
      new FormViewEcho(5.0F, 0.3F, 24.0F, 2.0F, 16.0F, 40.0F, 1.5F, 1.0F, 0.1F, 0.6F, 30, 0.6F);

  private static final CreatureRects.Packed NO_CREATURES =
      CreatureRects.pack(List.of(), new Matrix4f());

  private static FormView view(
      final Optional<FormViewEcho> echo,
      final Optional<FormViewTint> tint,
      final Optional<FormViewTremble> tremble,
      final Optional<FormViewWash> wash) {
    return new FormView(
        Identifier.of("nightborn", "test"), 0, 5, 5, 0.7F, echo, tint, tremble, wash);
  }

  private static Map<String, float[]> values(final FormView view, final List<Ripple> ripples) {
    return FormViewUniforms.of(view, 0.5F, 12.0, ripples, new Vec3d(10, 64, 10), NO_CREATURES);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "Strength", "Time", "Darkness", "EchoNear", "EchoLook", "Ripples", "RippleWaves",
        "CreatureRects", "CreatureDepths", "TintColor", "Tint", "Tremble", "WashColor", "Wash"
      })
  @DisplayName("every uniform the shader reads is filled, with or without each part")
  void fillsEveryUniform(final String name) {
    final FormView bare =
        view(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    assertEquals(true, values(bare, List.of()).containsKey(name));
  }

  @Test
  @DisplayName("the strength and darkness reach the shader as given")
  void strengthAndDarkness() {
    final Map<String, float[]> values =
        values(
            view(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            List.of());
    assertArrayEquals(new float[] {0.5F}, values.get("Strength"));
    assertArrayEquals(new float[] {0.7F}, values.get("Darkness"));
  }

  @Test
  @DisplayName("a view without an echo switches the echo off")
  void noEcho() {
    assertArrayEquals(
        new float[] {0.0F, 0.0F, 0.0F},
        values(
                view(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
                List.of())
            .get("EchoNear"));
  }

  @Test
  @DisplayName("a ripple is placed relative to the camera with its current ring and strength")
  void rippleRelativeToCamera() {
    final Ripple ripple = new Ripple(new Vec3d(12, 66, 14), 11.5, 30.0F, 1.0F);
    final Map<String, float[]> values =
        values(
            view(Optional.of(ECHO), Optional.empty(), Optional.empty(), Optional.empty()),
            List.of(ripple));
    final float[] ripples = values.get("Ripples");
    assertArrayEquals(
        new float[] {2.0F, 2.0F, 4.0F, 12.0F},
        new float[] {ripples[0], ripples[1], ripples[2], ripples[3]},
        1.0E-4F);
    final float[] waves = values.get("RippleWaves");
    assertEquals(0.75F, waves[0], 1.0E-4F);
    assertEquals(1.5F, waves[1], 1.0E-4F);
    assertEquals(0.0F, waves[2]);
    assertArrayEquals(new float[] {5.0F, 0.3F, 1.0F}, values.get("EchoNear"));
  }

  @Test
  @DisplayName("more ripples than the shader holds are cut to its limit")
  void ripplesCapped() {
    final List<Ripple> many =
        java.util.Collections.nCopies(50, new Ripple(Vec3d.ZERO, 12.0, 10.0F, 1.0F));
    final Map<String, float[]> values =
        values(view(Optional.of(ECHO), Optional.empty(), Optional.empty(), Optional.empty()), many);
    assertEquals(RippleField.CAPACITY * 4, values.get("Ripples").length);
    assertEquals(RippleField.CAPACITY * 2, values.get("RippleWaves").length);
  }

  @ParameterizedTest(name = "#{0}")
  @CsvSource({
    "FF0000, 1.0, 0.0, 0.0",
    "9E1418, 0.61960787, 0.078431375, 0.09411765",
    "000000, 0, 0, 0"
  })
  @DisplayName("a tint colour reaches the shader as red, green and blue from 0 to 1")
  void tintColour(final String hex, final float r, final float g, final float b) {
    final FormView tinted =
        view(
            Optional.empty(),
            Optional.of(new FormViewTint(Integer.parseInt(hex, 16), 0.2F, 0.6F)),
            Optional.empty(),
            Optional.empty());
    assertArrayEquals(new float[] {r, g, b}, values(tinted, List.of()).get("TintColor"), 1.0E-6F);
    assertArrayEquals(new float[] {0.2F, 0.6F}, values(tinted, List.of()).get("Tint"));
  }

  @Test
  @DisplayName("a view with no tremble or wash leaves the picture still and untouched")
  void stillWhenAbsent() {
    final Map<String, float[]> values =
        values(
            view(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            List.of());
    assertEquals(0.0F, values.get("Tremble")[0]);
    assertArrayEquals(new float[] {0.0F, 1.0F, 0.0F, 0.0F}, values.get("Wash"));
  }
}
