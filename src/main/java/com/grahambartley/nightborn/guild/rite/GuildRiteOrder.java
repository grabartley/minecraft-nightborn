package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GuildRiteOrder implements WheelAction<GuildMasterEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "guild_rite");
  public static final Identifier ICON = WheelEntry.icon("rite");

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return RiteAccess.offered(
        Vampires.isVampire(player),
        GuildMembership.isHunter(player),
        ServerConfigService.current().guild().riteNeedsOath());
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return List.of(WheelEntry.of(ID, ICON, Text.translatable("nightborn.guild.rite"), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final GuildMasterEntity master, final WheelEntry chosen) {
    GuildRites.ask(player, master);
  }
}
