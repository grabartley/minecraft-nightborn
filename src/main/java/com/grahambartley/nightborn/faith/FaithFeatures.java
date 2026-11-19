package com.grahambartley.nightborn.faith;

public final class FaithFeatures {

  private FaithFeatures() {}

  public static void register() {
    FaithMarks.register();
    Prayers.register();
    Kindling.register();
  }
}
