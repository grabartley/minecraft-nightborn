package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ChallengeRewards {

  static final String TRIAL_DONE = "nightborn.trial.done";
  static final String FEAT_DONE = "nightborn.feat.done";

  private ChallengeRewards() {}

  static void pay(
      final ServerPlayerEntity player,
      final ChallengeProgress.Outcome outcome,
      final ChallengeProgress.Context context) {
    for (final ChallengeProgress.TrialDone done : outcome.trials()) {
      final Trial trial = context.trials().get(done.trial());
      if (trial != null) {
        payTrial(player, trial, done.firstTime());
        Deeds.record(
            player,
            Deed.of(DeedKinds.TRIAL_COMPLETED)
                .with(DeedKinds.TRIAL, done.trial())
                .with(DeedKinds.SIDE, trial.side().asString())
                .with(
                    DeedKinds.DISTINCT,
                    outcome.record().finishedOf(trial.side(), context.trials())));
      }
    }
    for (final Identifier id : outcome.feats()) {
      final Feat feat = context.feats().get(id);
      if (feat != null) {
        payFeat(player, feat);
      }
    }
  }

  static Optional<SkillTree> tree(final Side side) {
    return switch (side) {
      case VAMPIRE -> Optional.of(SkillTree.VAMPIRE);
      case HUNTER -> Optional.of(SkillTree.HUNTER);
      case ANYONE -> Optional.empty();
    };
  }

  private static void payTrial(
      final ServerPlayerEntity player, final Trial trial, final boolean firstTime) {
    tree(trial.side())
        .ifPresent(
            tree ->
                SkillEarning.fromTrial(
                    player, tree, trial.size().share(), ServerConfigService.current().skills()));
    if (firstTime) {
      trial.reward().ifPresent(reward -> give(player, reward));
    }
    player.sendMessage(Text.translatable(TRIAL_DONE, trial.name()), false);
    player
        .getServerWorld()
        .playSound(
            null,
            player.getBlockPos(),
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
            SoundCategory.PLAYERS,
            0.6F,
            0.8F);
  }

  private static void payFeat(final ServerPlayerEntity player, final Feat feat) {
    give(player, feat.trophy());
    feat.reward().ifPresent(reward -> give(player, reward));
    player.sendMessage(Text.translatable(FEAT_DONE, feat.name(), feat.title()), false);
    player
        .getServerWorld()
        .playSound(
            null,
            player.getBlockPos(),
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
            SoundCategory.PLAYERS,
            1.0F,
            0.6F);
  }

  private static void give(final ServerPlayerEntity player, final Identifier item) {
    Registries.ITEM
        .getOrEmpty(item)
        .filter(found -> found != Items.AIR)
        .ifPresent(found -> player.getInventory().offerOrDrop(new ItemStack(found)));
  }
}
