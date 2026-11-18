package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class HouseLeave implements WheelAction<PlayerEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "house_leave");
  static final Identifier ICON = Identifier.of("minecraft", "oak_door");
  static final String LABEL = "nightborn.house.wheel.leave";

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final PlayerEntity lord) {
    return Houses.get(player.getServer())
        .ledBy(lord.getUuid())
        .filter(house -> house.players().contains(player.getUuid()))
        .isPresent();
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final PlayerEntity lord) {
    return List.of(WheelEntry.of(ID, ICON, Text.translatable(LABEL), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final PlayerEntity lord, final WheelEntry chosen) {
    HouseInvites.leave(player);
  }
}
