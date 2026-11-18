package com.grahambartley.nightborn.house;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public final class HouseFeatures {

  private HouseFeatures() {}

  public static void register() {
    HouseFounding.register();
    HouseInvites.register();
    HouseCommand.register();
    HouseDefence.register();
    BloodStore.register();
    HouseWheel.register();
    UseEntityCallback.EVENT.register(ChaliceInvitation::onUse);
  }
}
