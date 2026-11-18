package com.grahambartley.nightborn.client.boss;

public final class ArenaWallShape {

  static final double SEGMENT = 1.5;
  static final int FEWEST = 16;
  static final double SIGHT = 12.0;
  static final float FAINTEST = 0.15F;
  static final float STRONGEST = 0.75F;

  static final int ROWS = 6;
  static final float CALM = 1.0F;
  static final float ROUSED = 1.8F;
  static final double SWELL = 0.22;
  static final double SHIVER = 0.18;
  static final float BEAT_TICKS = 28.0F;
  static final float REST = 0.72F;
  static final int[] PALETTE = {0x8E0A14, 0x4A0A5E, 0x2A0008, 0x9C1050};
  static final double TURNS = 2.0;
  static final double CREEP = 0.004;
  static final double SURFACE_TICKS = 240.0;

  private ArenaWallShape() {}

  public static float strength(final boolean fighting) {
    return fighting ? ROUSED : CALM;
  }

  public static double ripple(
      final double angle, final double height, final double time, final float strength) {
    return strength
        * (SWELL * Math.sin(angle * 5.0 + time * 0.07)
            + SHIVER * Math.sin(height * 0.9 - time * 0.11 + angle * 3.0));
  }

  public static float pulse(final double time, final float beatTicks, final boolean breathes) {
    if (breathes) {
      final double breath = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * time / beatTicks);
      return (float) (REST + (1.0 - REST) * breath);
    }
    final double beat = (time % beatTicks) / beatTicks;
    final double first = Math.exp(-Math.pow((beat - 0.10) / 0.06, 2.0));
    final double second = Math.exp(-Math.pow((beat - 0.32) / 0.06, 2.0));
    return (float) (REST + (1.0 - REST) * Math.min(1.0, first + 0.7 * second));
  }

  public static float surfacing(final double time) {
    final double wave = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * time / SURFACE_TICKS);
    return (float) (wave * wave);
  }

  public static int tint(final int[] palette, final double angle, final double time) {
    final double turn = angle / (2.0 * Math.PI) * TURNS + time * CREEP;
    final double at = (turn - Math.floor(turn)) * palette.length;
    final int from = (int) Math.floor(at) % palette.length;
    final int to = (from + 1) % palette.length;
    final double blend = at - Math.floor(at);
    return mix(palette[from], palette[to], blend);
  }

  static int mix(final int from, final int to, final double blend) {
    int mixed = 0;
    for (int shift = 16; shift >= 0; shift -= 8) {
      final int a = from >> shift & 0xFF;
      final int b = to >> shift & 0xFF;
      mixed |= (int) Math.round(a + (b - a) * blend) << shift;
    }
    return mixed;
  }

  public static int segments(final int radius) {
    return Math.max(FEWEST, (int) Math.ceil(2.0 * Math.PI * radius / SEGMENT));
  }

  public static double x(final int segment, final int segments, final int radius) {
    return Math.cos(2.0 * Math.PI * segment / segments) * radius;
  }

  public static double z(final int segment, final int segments, final int radius) {
    return Math.sin(2.0 * Math.PI * segment / segments) * radius;
  }

  public static float alpha(final double fromWall) {
    final double near = Math.max(0.0, Math.min(1.0, 1.0 - Math.abs(fromWall) / SIGHT));
    return (float) (FAINTEST + (STRONGEST - FAINTEST) * near);
  }
}
