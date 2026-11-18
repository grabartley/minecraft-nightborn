package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class HouseInvite implements WheelAction<PlayerEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "house_invite");
  static final Identifier ICON = WheelEntry.icon("invite");
  static final String LABEL = "nightborn.house.wheel.invite";

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final PlayerEntity guest) {
    return guest instanceof ServerPlayerEntity
        && Lordship.judge(player, guest) == HouseRules.Invite.WELCOME;
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final PlayerEntity guest) {
    return List.of(WheelEntry.of(ID, ICON, Text.translatable(LABEL), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final PlayerEntity guest, final WheelEntry chosen) {
    if (guest instanceof ServerPlayerEntity invited) {
      HouseInvites.invite(player, invited);
    }
  }
}
