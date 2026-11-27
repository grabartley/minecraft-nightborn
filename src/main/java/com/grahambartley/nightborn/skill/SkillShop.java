package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.network.BuySkillPayload;
import com.grahambartley.nightborn.rank.GuildRanks;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SkillShop {

  private SkillShop() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        BuySkillPayload.ID, (payload, context) -> buy(context.player(), payload.skill()));
  }

  public static SkillPurchase.Verdict buy(final ServerPlayerEntity player, final Identifier id) {
    final DynamicRegistryManager registries = player.getServerWorld().getRegistryManager();
    final SkillTree tree = SkillTree.of(player);
    final TreeProgress progress = SkillAttachment.get(player, tree);
    final SkillLookup lookup =
        SkillLookup.of(
            registries,
            GuildMembership.sided(player),
            progress,
            tree,
            id,
            StageRanks.of(player),
            GuildRanks.of(player));
    if (lookup.verdict() != SkillPurchase.Verdict.ALLOWED) {
      player.sendMessage(Text.translatable(lookup.verdict().translationKey(tree)), true);
      return lookup.verdict();
    }
    final TreeProgress bought = SkillPurchase.apply(progress, id, lookup.skill(), lookup.branch());
    SkillAttachment.set(player, tree, bought);
    if (countsBloodlines(tree)) {
      MilestoneCriterion.reach(player, Milestone.BLOODLINES, bought.lines().size());
    }
    if (lookup.branch().line()) {
      final Identifier line = lookup.skill().branch();
      Deeds.record(
          player,
          Deed.of(DeedKinds.SKILL_LEARNED)
              .with(DeedKinds.TREE, tree.asString())
              .with(DeedKinds.LINE, line)
              .with(
                  DeedKinds.SHARE, LineShare.percent(Skills.allSkills(registries), bought, line)));
    }
    SkillAttributes.apply(player);
    player.sendMessage(bought(id, bought.rank(id), lookup.skill().maxRank()), true);
    player
        .getServerWorld()
        .playSound(
            null,
            player.getBlockPos(),
            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
            SoundCategory.PLAYERS,
            0.8F,
            0.7F);
    return lookup.verdict();
  }

  static boolean countsBloodlines(final SkillTree tree) {
    return tree == SkillTree.VAMPIRE;
  }

  static Text bought(final Identifier id, final int rank, final int maxRank) {
    final Text name = Text.translatable(SkillNames.skill(id));
    return maxRank > 1
        ? Text.translatable("nightborn.skills.bought.rank", name, rank)
        : Text.translatable("nightborn.skills.bought", name);
  }
}
