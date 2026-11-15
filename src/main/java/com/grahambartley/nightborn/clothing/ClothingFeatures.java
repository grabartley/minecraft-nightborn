package com.grahambartley.nightborn.clothing;

public final class ClothingFeatures {

  private ClothingFeatures() {}

  public static void register() {
    ClothingItems.register();
    NightSightEffect.register();
  }
}
