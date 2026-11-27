package com.grahambartley.nightborn.client.anim;

public record FirstPersonSway(float pitchDegrees, float yawDegrees, float reach) {

  public static final FirstPersonSway NONE = new FirstPersonSway(0.0F, 0.0F, 0.0F);

  static final double SHARE = 0.5;
  static final double FORWARD_RAISE_DEGREES = 80.0;
  static final double FADE_SECONDS = 0.25;
  static final double LIMIT_DEGREES = 45.0;
  static final double REACH_PER_PIXEL = 0.05;

  public static FirstPersonSway of(
      final double raiseRadians, final double flareRadians, final double forwardPixels) {
    return new FirstPersonSway(
        limited((Math.toDegrees(raiseRadians) - FORWARD_RAISE_DEGREES) * SHARE),
        limited(-Math.toDegrees(flareRadians) * SHARE),
        (float) (forwardPixels * REACH_PER_PIXEL));
  }

  public static double weight(
      final double clipSeconds, final double lengthSeconds, final double blendSeconds) {
    final double in = (clipSeconds + blendSeconds) / blendSeconds;
    final double out = (lengthSeconds - clipSeconds) / FADE_SECONDS;
    return Math.max(0.0, Math.min(1.0, Math.min(in, out)));
  }

  public FirstPersonSway scaled(final double share) {
    return new FirstPersonSway(
        (float) (pitchDegrees * share), (float) (yawDegrees * share), (float) (reach * share));
  }

  public boolean still() {
    return pitchDegrees == 0.0F && yawDegrees == 0.0F && reach == 0.0F;
  }

  private static float limited(final double degrees) {
    return (float) Math.max(-LIMIT_DEGREES, Math.min(LIMIT_DEGREES, degrees));
  }
}
