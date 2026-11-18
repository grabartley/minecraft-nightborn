package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.journal.Journal;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GuildTrialOrder implements WheelAction<GuildMasterEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "guild_trial");
  static final String TAKE = "nightborn.guild.trial";
  static final String TAKEN = "nightborn.guild.trial.taken";

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return !untaken(player).isEmpty();
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final GuildMasterEntity master) {
    final List<WheelEntry> entries = new ArrayList<>();
    final List<OfferedTrial> offered = ChallengeAttachment.get(player).offer().trials();
    for (int index = 0; index < offered.size(); index++) {
      final OfferedTrial trial = offered.get(index);
      if (!trial.taken()) {
        final Text name =
            name(player, trial.trial()).orElse(Text.literal(trial.trial().toString()));
        entries.add(
            new WheelEntry(ID, index, Journal.HUNTER.id(), Text.translatable(TAKE, name), false));
      }
    }
    return entries;
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final GuildMasterEntity master, final WheelEntry chosen) {
    final ChallengeRecord record = ChallengeAttachment.get(player);
    TrialTaking.take(record, Deeds.side(player), chosen.argument())
        .ifPresent(
            taken -> {
              ChallengeAttachment.set(player, taken.record());
              master.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0F, master.getSoundPitch());
              player.sendMessage(
                  Text.translatable(
                      TAKEN,
                      name(player, taken.trial()).orElse(Text.literal(taken.trial().toString()))),
                  false);
            });
  }

  private static List<OfferedTrial> untaken(final ServerPlayerEntity player) {
    if (Deeds.side(player).filter(Side.HUNTER::equals).isEmpty()) {
      return List.of();
    }
    return ChallengeAttachment.get(player).offer().trials().stream()
        .filter(trial -> !trial.taken())
        .toList();
  }

  private static Optional<Text> name(final ServerPlayerEntity player, final Identifier trial) {
    return Optional.ofNullable(ChallengeData.trials(player.getRegistryManager()).get(trial))
        .map(Trial::name);
  }
}
