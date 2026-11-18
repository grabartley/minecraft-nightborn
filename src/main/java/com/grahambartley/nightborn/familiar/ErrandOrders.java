package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.servant.OrderCue;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ErrandOrders implements WheelAction<HumanFamiliarEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "errands");
  public static final Identifier ICON = WheelEntry.icon("carry");

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final HumanFamiliarEntity familiar) {
    return familiar.isOwnedBy(player);
  }

  @Override
  public List<WheelEntry> entries(
      final ServerPlayerEntity player, final HumanFamiliarEntity familiar) {
    return List.of(
        WheelEntry.of(
            ID, ICON, Text.translatable("nightborn.familiar.errands"), familiar.job().isEmpty()));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player,
      final HumanFamiliarEntity familiar,
      final WheelEntry chosen) {
    familiar.dismiss();
    OrderCue.obey(
        familiar,
        player,
        Text.translatable("nightborn.familiar.errands.ordered", familiar.getDisplayName()));
  }
}
