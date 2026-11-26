package com.grahambartley.nightborn.client.anim;

public record FirstPersonSway(float pitchDegrees, float yawDegrees) {

  public static final FirstPersonSway NONE = new FirstPersonSway(0.0F, 0.0F);

  static final double SHARE = 0.5;
  static final double LIMIT_DEGREES = 45.0;

  public static FirstPersonSway of(final double raiseRadians, final double flareRadians) {
    return new FirstPersonSway(
        limited(Math.toDegrees(raiseRadians) * SHARE),
        limited(-Math.toDegrees(flareRadians) * SHARE));
  }

  public FirstPersonSway scaled(final double share) {
    return new FirstPersonSway((float) (pitchDegrees * share), (float) (yawDegrees * share));
  }

  public boolean still() {
    return pitchDegrees == 0.0F && yawDegrees == 0.0F;
  }

  private static float limited(final double degrees) {
    return (float) Math.max(-LIMIT_DEGREES, Math.min(LIMIT_DEGREES, degrees));
  }
}
