package com.grahambartley.nightborn.client.wheel;

import com.grahambartley.nightborn.network.OpenWheelPayload;
import com.grahambartley.nightborn.screen.CommandWheelScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class WheelDialogue {

  private WheelDialogue() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        OpenWheelPayload.ID,
        (payload, context) -> context.client().setScreen(new CommandWheelScreen(payload)));
  }
}
