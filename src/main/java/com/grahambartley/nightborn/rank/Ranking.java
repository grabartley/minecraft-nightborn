package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.faction.Oath;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.name.StageNames;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Collection;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class Ranking {

  static final int SETTLE_TICKS = 20;
  static final String RISEN = "nightborn.rank.risen";

  private Ranking() {}

  static void register() {
    ServerConfigService.onChange(
        (server, config) -> server.getPlayerManager().getPlayerList().forEach(Ranking::settle));
  }

  public static void tick(final ServerPlayerEntity player) {
    if (player.age % SETTLE_TICKS == 0) {
      settle(player);
      RankBody.apply(player, ServerConfigService.current().skills());
    }
  }

  public static void sworn(final ServerPlayerEntity player) {
    settle(player);
    changed(player);
  }

  public static void did(
      final ServerPlayerEntity player, final Collection<RankDeed.Act> acts, final int stageRank) {
    if (Vampires.isVampire(player)) {
      return;
    }
    final RankLadder ladder = RankLadder.of(player.getRegistryManager());
    OathAttachment.set(player, RankRules.did(OathAttachment.get(player), ladder, acts, stageRank));
    settle(player);
  }

  public static void settle(final ServerPlayerEntity player) {
    final Oath before = OathAttachment.get(player);
    if (!GuildMembership.isHunter(player)) {
      return;
    }
    final SkillSettings settings = ServerConfigService.current().skills();
    final RankLadder ladder = RankLadder.of(player.getRegistryManager());
    final Oath settled =
        RankRules.advance(
            before,
            Vampires.isVampire(player),
            SkillAttachment.get(player, SkillTree.HUNTER).earned(),
            ladder,
            settings.rankNeeds());
    if (settled.rank() <= before.rank()) {
      return;
    }
    OathAttachment.set(player, settled);
    changed(player);
    for (int rank = before.rank() + 1; rank <= settled.rank(); rank++) {
      ladder.rung(rank).ifPresent(rung -> herald(player, rung));
    }
  }

  public static boolean setRank(final ServerPlayerEntity player, final int rank) {
    final Oath oath = OathAttachment.get(player);
    if (!GuildMembership.isHunter(player) || oath.rank() == rank) {
      return false;
    }
    OathAttachment.set(player, oath.withRank(rank));
    changed(player);
    return true;
  }

  private static void changed(final ServerPlayerEntity player) {
    RankBody.apply(player, ServerConfigService.current().skills());
    StageNames.changed(player);
  }

  private static void herald(final ServerPlayerEntity player, final RankLadder.Rung rung) {
    player.sendMessage(Text.translatable(RISEN, rung.rank().name()), false);
    player
        .getServerWorld()
        .playSound(
            null,
            player.getBlockPos(),
            SoundEvents.BLOCK_BELL_USE,
            SoundCategory.PLAYERS,
            1.0F,
            0.8F);
  }
}
