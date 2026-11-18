package com.grahambartley.nightborn.screen;

public final class WheelRing {

  public static final int OUTER = 92;
  public static final int INNER = 40;
  public static final int MARGIN = 4;
  public static final float GAP_DEGREES = 1.5F;
  public static final int RIM = 4;

  private WheelRing() {}

  public static int sectorAt(final double dx, final double dy, final int count) {
    if (count <= 0) {
      return -1;
    }
    final double distanceSquared = dx * dx + dy * dy;
    final double inner = INNER - MARGIN;
    final double outer = OUTER + MARGIN;
    if (distanceSquared < inner * inner || distanceSquared > outer * outer) {
      return -1;
    }
    final double span = 2 * Math.PI / count;
    final double fromTop = Math.atan2(dy, dx) + Math.PI / 2 + span / 2;
    final double turned = ((fromTop % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI);
    return (int) (turned / span) % count;
  }

  public static float startDegrees(final int index, final int count) {
    final float span = 360.0F / count;
    return -90.0F + (index - 0.5F) * span + GAP_DEGREES / 2.0F;
  }

  public static float endDegrees(final int index, final int count) {
    return startDegrees(index, count) + 360.0F / count - GAP_DEGREES;
  }

  public static boolean drawsOverlay(final int count, final int hovered) {
    return count > 1 || hovered >= 0;
  }

  public static boolean hasSpokes(final int count) {
    return count > 1;
  }

  public static float spokeStartDegrees(final int index, final int count) {
    return startDegrees(index, count) - GAP_DEGREES;
  }

  public static int glowInner() {
    return INNER + RIM;
  }

  public static int glowOuter() {
    return OUTER - RIM;
  }

  public static int iconX(final int index, final int count) {
    return (int) Math.round(middle() * Math.cos(centreRadians(index, count)));
  }

  public static int iconY(final int index, final int count) {
    return (int) Math.round(middle() * Math.sin(centreRadians(index, count)));
  }

  private static double middle() {
    return (INNER + OUTER) / 2.0;
  }

  private static double centreRadians(final int index, final int count) {
    return -Math.PI / 2 + index * 2 * Math.PI / count;
  }

  public static int numberKey(final int keyOffset, final int count) {
    return keyOffset >= 0 && keyOffset < count ? keyOffset : -1;
  }
}
