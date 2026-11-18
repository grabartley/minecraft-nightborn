package com.grahambartley.nightborn.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class NightbornPayloads {

  private NightbornPayloads() {}

  public static void register() {
    PayloadTypeRegistry.playS2C().register(SyncConfigPayload.ID, SyncConfigPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(StructureZonePayload.ID, StructureZonePayload.CODEC);
    PayloadTypeRegistry.playS2C().register(PlayerCuePayload.ID, PlayerCuePayload.CODEC);
    PayloadTypeRegistry.playS2C().register(GorePayload.ID, GorePayload.CODEC);
    PayloadTypeRegistry.playS2C().register(RitualGloomPayload.ID, RitualGloomPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(BossArenaPayload.ID, BossArenaPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(OpenNamingPayload.ID, OpenNamingPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(StageNamePayload.ID, StageNamePayload.CODEC);
    PayloadTypeRegistry.playS2C().register(HouseNamePayload.ID, HouseNamePayload.CODEC);
    PayloadTypeRegistry.playS2C().register(OpenHouseNamingPayload.ID, OpenHouseNamingPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(MirrorPayload.ID, MirrorPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(OpenWheelPayload.ID, OpenWheelPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(EditConfigPayload.ID, EditConfigPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(BuySkillPayload.ID, BuySkillPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(UsePowerPayload.ID, UsePowerPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(SetPowerSlotPayload.ID, SetPowerSlotPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(ToggleLevitationPayload.ID, ToggleLevitationPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(ToggleTurningBitePayload.ID, ToggleTurningBitePayload.CODEC);
    PayloadTypeRegistry.playC2S().register(ReleaseBatHangPayload.ID, ReleaseBatHangPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(NameServantPayload.ID, NameServantPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(NameHousePayload.ID, NameHousePayload.CODEC);
    PayloadTypeRegistry.playC2S().register(ChooseWheelPayload.ID, ChooseWheelPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(ChooseTitlePayload.ID, ChooseTitlePayload.CODEC);
  }
}
