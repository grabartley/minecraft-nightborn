package com.grahambartley.nightborn.seeker;

public final class SeekerFeatures {

  private SeekerFeatures() {}

  public static void register() {
    Quarries.register();
    Revelations.register();
    TrueSights.register();
    SixthSense.register();
    GraveListener.register();
    Scenes.register();
  }
}
