package com.grahambartley.nightborn.enchant;

public final class EnchantFeatures {

  private EnchantFeatures() {}

  public static void register() {
    VampireCondition.register();
    HasBloodCondition.register();
    TimeOfDayCondition.register();
    EnchantedValues.register();
    BloodlettingEffect.register();
  }
}
