package com.grahambartley.nightborn.inquisitor;

import software.bernie.geckolib.animation.RawAnimation;

public enum HawkFlight {
  FLY("fly"),
  GLIDE("glide");

  static final double CLIMB = 0.02;

  private final RawAnimation clip;

  HawkFlight(final String animation) {
    this.clip = RawAnimation.begin().thenLoop(animation);
  }

  public static HawkFlight of(final double rise) {
    return rise > CLIMB ? FLY : GLIDE;
  }

  public RawAnimation clip() {
    return clip;
  }
}
