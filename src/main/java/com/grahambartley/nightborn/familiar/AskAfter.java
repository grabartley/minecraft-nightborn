package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class AskAfter implements WheelAction<HumanFamiliarEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "ask_after");
  public static final Identifier ICON = WheelEntry.icon("ask_after");

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
        WheelEntry.of(ID, ICON, Text.translatable("nightborn.familiar.ask_after"), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player,
      final HumanFamiliarEntity familiar,
      final WheelEntry chosen) {
    player.sendMessage(
        Text.translatable(FamiliarMood.of(familiar.loyalty(), familiar.turned()).translationKey()),
        true);
  }
}
