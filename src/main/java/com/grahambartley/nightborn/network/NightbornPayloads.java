package com.grahambartley.nightborn.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class NightbornPayloads {

  private NightbornPayloads() {}

  public static void register() {
    PayloadTypeRegistry.playS2C().register(SyncConfigPayload.ID, SyncConfigPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(StructureFogPayload.ID, StructureFogPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(EditConfigPayload.ID, EditConfigPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(BuySkillPayload.ID, BuySkillPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(UsePowerPayload.ID, UsePowerPayload.CODEC);
  }
}
