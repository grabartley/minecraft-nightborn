package com.grahambartley.nightborn.inquisitor;

public final class InquisitorFeatures {

  private InquisitorFeatures() {}

  public static void register() {
    InquisitorMobs.register();
    InquisitorSpoils.register();
    InquisitorSet.register();
    InquisitorOutcomes.register();
    InquisitorDeaths.register();
    InquisitorDues.register();
  }
}
