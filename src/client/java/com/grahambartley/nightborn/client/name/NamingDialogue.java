package com.grahambartley.nightborn.client.name;

import com.grahambartley.nightborn.network.OpenHouseNamingPayload;
import com.grahambartley.nightborn.network.OpenNamingPayload;
import com.grahambartley.nightborn.screen.HouseNamingScreen;
import com.grahambartley.nightborn.screen.NamingScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class NamingDialogue {

  private NamingDialogue() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        OpenNamingPayload.ID,
        (payload, context) ->
            context.client().setScreen(new NamingScreen(payload.entityId(), payload.suggestion())));
    ClientPlayNetworking.registerGlobalReceiver(
        OpenHouseNamingPayload.ID,
        (payload, context) ->
            context
                .client()
                .setScreen(new HouseNamingScreen(payload.suggestion(), payload.crest())));
  }
}
