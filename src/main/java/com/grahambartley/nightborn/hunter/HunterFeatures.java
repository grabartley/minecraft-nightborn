package com.grahambartley.nightborn.hunter;

public final class HunterFeatures {

  private HunterFeatures() {}

  public static void register() {
    HunterAttributes.register();
    HuntRewards.register();
  }
}
