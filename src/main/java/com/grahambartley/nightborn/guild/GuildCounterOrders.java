package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import java.util.Locale;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GuildCounterOrders implements WheelAction<GuildMasterEntity> {

  private final GuildCounter.Kind kind;
  private final Identifier id;

  public GuildCounterOrders(final GuildCounter.Kind kind) {
    this.kind = kind;
    this.id = Identifier.of(Nightborn.MOD_ID, "guild_" + kind.name().toLowerCase(Locale.ROOT));
  }

  @Override
  public Identifier id() {
    return id;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return !Vampires.isVampire(player);
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return List.of(
        WheelEntry.of(
            id, Identifier.of(kind.icon()), Text.translatable(kind.translationKey()), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final GuildMasterEntity master, final WheelEntry chosen) {
    master.openCounter(player, kind);
  }
}
