package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.faction.Oath;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.rank.RankOutlook;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GuildRankOrder implements WheelAction<GuildMasterEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "guild_rank");
  public static final Identifier ICON = WheelEntry.icon("rank");

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return GuildMembership.isHunter(player);
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final GuildMasterEntity master) {
    return List.of(WheelEntry.of(ID, ICON, Text.translatable("nightborn.guild.rank"), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final GuildMasterEntity master, final WheelEntry chosen) {
    final Oath oath = OathAttachment.get(player);
    final RankLadder ladder = RankLadder.of(player.getRegistryManager());
    master.playSound(SoundEvents.ENTITY_VILLAGER_AMBIENT, 1.0F, master.getSoundPitch());
    told(
            master.getDisplayName(),
            ladder.rung(oath.rank()).map(rung -> rung.rank().name()),
            RankOutlook.of(
                oath,
                SkillAttachment.get(player, SkillTree.HUNTER).earned(),
                ladder,
                ServerConfigService.current().skills().rankNeeds()))
        .forEach(line -> player.sendMessage(line, false));
  }

  static List<Text> told(
      final Text teller, final Optional<Text> rank, final Optional<RankOutlook> next) {
    final List<Text> lines = new ArrayList<>();
    lines.add(
        Text.translatable(
            "nightborn.rank.told", teller, rank.orElse(Text.translatable("nightborn.rank.none"))));
    next.ifPresentOrElse(
        outlook -> {
          final Text name = outlook.next().rank().name();
          lines.add(
              outlook.pointsLeft() > 0
                  ? Text.translatable("nightborn.rank.next", name, outlook.pointsLeft())
                  : Text.translatable("nightborn.rank.next.earned", name));
          outlook
              .deed()
              .ifPresent(
                  deed ->
                      lines.add(
                          Text.translatable(
                              outlook.deedDone()
                                  ? "nightborn.rank.deed.done"
                                  : "nightborn.rank.deed",
                              deed.description())));
        },
        () -> lines.add(Text.translatable("nightborn.rank.top")));
    return lines;
  }
}
