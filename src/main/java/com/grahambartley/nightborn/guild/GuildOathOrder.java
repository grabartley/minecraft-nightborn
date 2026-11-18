package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import java.util.Optional;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GuildOathOrder implements WheelAction<GuildMasterEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "guild_oath");

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return GuildMembership.maySwear(player);
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return List.of(
        WheelEntry.of(
            ID,
            Registries.ITEM.getId(GearItems.GOLD_CRUCIFIX),
            Text.translatable("nightborn.guild.oath"),
            false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final GuildMasterEntity master, final WheelEntry chosen) {
    GuildOath.swear(player, Optional.of(master));
  }
}
