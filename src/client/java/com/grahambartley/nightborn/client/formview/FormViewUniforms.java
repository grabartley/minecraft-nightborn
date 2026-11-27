package com.grahambartley.nightborn.client.formview;

import com.grahambartley.nightborn.formview.FormView;
import com.grahambartley.nightborn.formview.FormViewEcho;
import com.grahambartley.nightborn.formview.FormViewTint;
import com.grahambartley.nightborn.formview.FormViewTremble;
import com.grahambartley.nightborn.formview.FormViewWash;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.Vec3d;

public final class FormViewUniforms {

  private static final float CHANNEL = 255.0F;

  private FormViewUniforms() {}

  public static Map<String, float[]> of(
      final FormView view,
      final float strength,
      final double nowSeconds,
      final List<Ripple> ripples,
      final Vec3d camera,
      final CreatureRects.Packed creatures) {
    final Map<String, float[]> values = new LinkedHashMap<>();
    values.put("Strength", new float[] {strength});
    values.put("Time", new float[] {(float) (nowSeconds % 3600.0)});
    values.put("Darkness", new float[] {view.darkness()});
    echo(values, view, nowSeconds, ripples, camera, creatures);
    final FormViewTint tint = view.tint().orElse(new FormViewTint(0, 0.0F, 0.0F));
    values.put("TintColor", rgb(tint.color()));
    values.put("Tint", new float[] {tint.centre(), tint.edge()});
    final FormViewTremble tremble = view.tremble().orElse(new FormViewTremble(0.0F, 1.0F, 0.0F));
    values.put("Tremble", new float[] {tremble.amount(), tremble.inner(), tremble.speed()});
    final FormViewWash wash = view.wash().orElse(new FormViewWash(0, 0.0F, 1.0F, 0.0F, 0.0F));
    values.put("WashColor", rgb(wash.color()));
    values.put("Wash", new float[] {wash.amount(), wash.saturation(), wash.blur(), wash.drift()});
    return values;
  }

  private static void echo(
      final Map<String, float[]> values,
      final FormView view,
      final double nowSeconds,
      final List<Ripple> ripples,
      final Vec3d camera,
      final CreatureRects.Packed creatures) {
    final float[] positions = new float[RippleField.CAPACITY * 4];
    final float[] waves = new float[RippleField.CAPACITY * 2];
    if (view.echo().isEmpty()) {
      values.put("EchoNear", new float[] {0.0F, 0.0F, 0.0F});
      values.put("EchoLook", new float[] {0.0F, 0.0F, 0.0F});
      values.put("Ripples", positions);
      values.put("RippleWaves", waves);
      values.put("CreatureRects", new float[CreatureRects.CAPACITY * 4]);
      values.put("CreatureDepths", new float[CreatureRects.CAPACITY * 2]);
      return;
    }
    final FormViewEcho echo = view.echo().get();
    final int count = Math.min(RippleField.CAPACITY, ripples.size());
    for (int i = 0; i < count; i++) {
      final Ripple ripple = ripples.get(i);
      final Vec3d at = ripple.origin().subtract(camera);
      positions[i * 4] = (float) at.x;
      positions[i * 4 + 1] = (float) at.y;
      positions[i * 4 + 2] = (float) at.z;
      positions[i * 4 + 3] = ripple.radius(nowSeconds, echo.rippleSpeed());
      waves[i * 2] = ripple.alpha(nowSeconds, echo.rippleSeconds());
      waves[i * 2 + 1] = echo.rippleWidth();
    }
    values.put("EchoNear", new float[] {echo.nearRange(), echo.nearBrightness(), 1.0F});
    values.put("EchoLook", new float[] {echo.outline(), echo.fill(), echo.creatureBoost()});
    values.put("Ripples", positions);
    values.put("RippleWaves", waves);
    values.put("CreatureRects", creatures.rects());
    values.put("CreatureDepths", creatures.depths());
  }

  static float[] rgb(final int color) {
    return new float[] {
      ((color >> 16) & 0xFF) / CHANNEL, ((color >> 8) & 0xFF) / CHANNEL, (color & 0xFF) / CHANNEL
    };
  }
}
