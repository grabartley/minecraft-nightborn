package com.grahambartley.nightborn.trap;

public final class TrapFeatures {

  private TrapFeatures() {}

  public static void register() {
    TrapEntities.initialize();
    Grounding.register();
    KillingGrounds.register();
    Baits.register();
    TrapSense.register();
  }
}
