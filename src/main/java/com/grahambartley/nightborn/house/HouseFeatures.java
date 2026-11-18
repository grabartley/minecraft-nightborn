package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.house.crest.CrestBanners;
import com.grahambartley.nightborn.house.duel.HouseDuels;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public final class HouseFeatures {

  private HouseFeatures() {}

  public static void register() {
    CrestBanners.initialize();
    HouseFounding.register();
    HouseInvites.register();
    HouseCommand.register();
    HouseDefence.register();
    BloodStore.register();
    HouseWheel.register();
    HouseRanks.register();
    HouseSuccession.register();
    HouseDuels.register();
    HouseNaming.register();
    HouseNames.register();
    UseEntityCallback.EVENT.register(ChaliceInvitation::onUse);
  }
}
