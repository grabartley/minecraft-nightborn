package com.grahambartley.nightborn.village;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public final class VillageFeatures {

  private VillageFeatures() {}

  public static void register() {
    VillageData.register();
    WitnessReports.register();
    VillageWatch.register();
    UseEntityCallback.EVENT.register(TradeRefusal::onUse);
  }
}
