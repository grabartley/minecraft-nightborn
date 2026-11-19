package com.grahambartley.nightborn.clothing;

public final class ClothingFeatures {

  private ClothingFeatures() {}

  public static void register() {
    WornAttributes.register();
    WornPowers.register();
    ClothingItems.register();
    Lining.register();
    NightSightEffect.register();
    ClothingPowerEffect.register();
  }
}
