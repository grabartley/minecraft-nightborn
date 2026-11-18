package com.grahambartley.nightborn.clothing;

public final class ClothingFeatures {

  private ClothingFeatures() {}

  public static void register() {
    WornAttributes.register();
    WornPowers.register();
    ClothingItems.register();
    NightSightEffect.register();
    ClothingPowerEffect.register();
  }
}
