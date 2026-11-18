package com.grahambartley.nightborn.hunt;

public final class HuntFeatures {

  private HuntFeatures() {}

  public static void register() {
    HuntAttachment.register();
    HuntSounds.initialize();
    HuntItems.register();
    HuntData.register();
    StandIn.register();
    NotorietyAward.register();
  }
}
