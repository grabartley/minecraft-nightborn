package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.journal.Journal;
import com.grahambartley.nightborn.journal.Journals;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GuildJournalOrder implements WheelAction<GuildMasterEntity> {

  public static final Identifier ICON = WheelEntry.icon("journal");
  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "guild_journal");

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return GuildMembership.isHunter(player) && !Journals.carries(player, Journal.HUNTER);
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return List.of(WheelEntry.of(ID, ICON, Text.translatable("nightborn.guild.journal"), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final GuildMasterEntity master, final WheelEntry chosen) {
    master.playSound(SoundEvents.ENTITY_VILLAGER_AMBIENT, 1.0F, master.getSoundPitch());
    Journals.give(player, Journal.HUNTER);
  }
}
