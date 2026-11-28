package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class HunterTradeOrder implements WheelAction<HunterEntity> {

  static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "hunter_trade");

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final HunterEntity hunter) {
    return HunterReferral.answers(hunter, player);
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final HunterEntity hunter) {
    return List.of(
        WheelEntry.of(
            ID,
            GuildCounter.Kind.GEAR.icon(),
            Text.translatable(GuildCounter.Kind.GEAR.translationKey()),
            false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final HunterEntity hunter, final WheelEntry chosen) {
    hunter.wares().open(player);
  }
}
