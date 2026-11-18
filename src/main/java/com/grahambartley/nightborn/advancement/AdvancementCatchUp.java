package com.grahambartley.nightborn.advancement;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.skill.LineShare;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class AdvancementCatchUp {

  public record LineShareDone(SkillTree tree, Identifier line, int share) {}

  private AdvancementCatchUp() {}

  public static void register() {
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> catchUp(handler.getPlayer()));
  }

  static void catchUp(final ServerPlayerEntity player) {
    final DynamicRegistryManager registries = player.getRegistryManager();
    final List<Identifier> stages =
        Vampires.isVampire(player)
            ? reached(
                AgeAttachment.get(player).rank(),
                AgeLadder.of(registries).rungs().stream().map(AgeLadder.Rung::id).toList())
            : List.of();
    final List<Identifier> ranks =
        GuildMembership.isHunter(player)
            ? reached(
                OathAttachment.get(player).rank(),
                RankLadder.of(registries).rungs().stream().map(RankLadder.Rung::id).toList())
            : List.of();
    final List<LineShareDone> shares = new ArrayList<>();
    final var skills = Skills.allSkills(registries);
    Skills.allBranches(registries)
        .forEach(
            (id, branch) -> {
              if (branch.line()) {
                shares.add(
                    new LineShareDone(
                        branch.tree(),
                        id,
                        LineShare.percent(skills, SkillAttachment.get(player, branch.tree()), id)));
              }
            });
    final boolean leadsHouse = Houses.get(player.getServer()).ledBy(player.getUuid()).isPresent();
    deeds(stages, ranks, shares, leadsHouse).forEach(deed -> DeedCriterion.done(player, deed));
  }

  static List<Identifier> reached(final int rank, final List<Identifier> ladder) {
    return ladder.subList(0, Math.max(0, Math.min(rank + 1, ladder.size())));
  }

  static List<Deed> deeds(
      final List<Identifier> stages,
      final List<Identifier> ranks,
      final List<LineShareDone> shares,
      final boolean leadsHouse) {
    final List<Deed> deeds = new ArrayList<>();
    stages.forEach(
        stage -> deeds.add(Deed.of(DeedKinds.STAGE_REACHED).with(DeedKinds.STAGE, stage)));
    ranks.forEach(rank -> deeds.add(Deed.of(DeedKinds.RANK_REACHED).with(DeedKinds.RANK, rank)));
    shares.stream()
        .filter(done -> done.share() > 0)
        .forEach(
            done ->
                deeds.add(
                    Deed.of(DeedKinds.SKILL_LEARNED)
                        .with(DeedKinds.TREE, done.tree().asString())
                        .with(DeedKinds.LINE, done.line())
                        .with(DeedKinds.SHARE, done.share())));
    if (leadsHouse) {
      deeds.add(Deed.of(DeedKinds.HOUSE_FOUNDED));
    }
    return deeds;
  }
}
