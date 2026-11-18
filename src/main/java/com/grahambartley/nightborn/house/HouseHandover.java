package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class HouseHandover implements WheelAction<PlayerEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "house_handover");
  static final Identifier ICON = Identifier.of(Nightborn.MOD_ID, "crest_banner");
  static final String LABEL = "nightborn.house.wheel.handover";

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity lord, final PlayerEntity member) {
    return Houses.get(lord.getServer())
        .ledBy(lord.getUuid())
        .map(house -> HouseRanks.refreshed(lord.getServer(), house))
        .filter(house -> HouseSuccession.mayHandOver(house, member.getUuid()))
        .isPresent();
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity lord, final PlayerEntity member) {
    return List.of(
        WheelEntry.of(ID, ICON, Text.translatable(LABEL, member.getDisplayName()), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity lord, final PlayerEntity member, final WheelEntry chosen) {
    if (member instanceof ServerPlayerEntity heir) {
      HouseSuccession.handOver(lord, heir);
    }
  }
}
