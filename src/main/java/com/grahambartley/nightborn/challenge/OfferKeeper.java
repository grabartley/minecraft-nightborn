package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.accessory.AccessoryItem;
import com.grahambartley.nightborn.accessory.AccessoryOwnership;
import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.config.ChallengeSettings;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class OfferKeeper {

  static final int CHECK_TICKS = 100;
  static final String RENEWED = "nightborn.trial.renewed.";

  private OfferKeeper() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(OfferKeeper::tick);
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> refresh(handler.getPlayer()));
  }

  private static void tick(final MinecraftServer server) {
    if (server.getTicks() % CHECK_TICKS != 0) {
      return;
    }
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      refresh(player);
      countAccessories(player);
    }
  }

  public static void refresh(final ServerPlayerEntity player) {
    final MinecraftServer server = player.getServer();
    if (server == null || player.isRemoved()) {
      return;
    }
    final ChallengeSettings settings = ServerConfigService.current().challenges();
    final Optional<Side> side = Deeds.side(player);
    final long period =
        ChallengeClock.period(server.getOverworld().getTimeOfDay(), settings.trialDays());
    final ChallengeRecord record = ChallengeAttachment.get(player);
    final TrialEligibility eligibility = eligibility(player, side);
    final List<TrialDraw.Candidate> open =
        ChallengeData.trials(player.getRegistryManager()).entrySet().stream()
            .filter(entry -> eligibility.open(entry.getValue()))
            .map(Map.Entry::getKey)
            .map(id -> new TrialDraw.Candidate(id, record.everFinished(id)))
            .toList();
    TrialDraw.renewed(
            record.offer(),
            side,
            period,
            open,
            settings.trialsOnOffer(),
            player.getUuid().getMostSignificantBits() ^ period)
        .ifPresent(
            offer -> {
              ChallengeAttachment.set(player, record.withOffer(offer));
              if (!offer.trials().isEmpty() && offer.side().isPresent()) {
                player.sendMessage(
                    Text.translatable(RENEWED + offer.side().get().asString()), true);
              }
            });
  }

  static TrialEligibility eligibility(final ServerPlayerEntity player, final Optional<Side> side) {
    final boolean vampire = side.filter(Side.VAMPIRE::equals).isPresent();
    final TreeProgress skills =
        SkillAttachment.get(player, vampire ? SkillTree.VAMPIRE : SkillTree.HUNTER);
    if (vampire) {
      final AgeLadder ladder = AgeLadder.of(player.getRegistryManager());
      return new TrialEligibility(
          side,
          AgeAttachment.get(player).rank(),
          gate -> ladder.rankOf(gate).map(OptionalInt::of).orElse(OptionalInt.empty()),
          skills::owns,
          DeedKinds.live());
    }
    final RankLadder ladder = RankLadder.of(player.getRegistryManager());
    return new TrialEligibility(
        side, OathAttachment.get(player).rank(), ladder::rankOf, skills::owns, DeedKinds.live());
  }

  private static void countAccessories(final ServerPlayerEntity player) {
    final long held =
        AccessoryOwnership.items(player).stream().filter(AccessoryItem.class::isInstance).count();
    if (held > 0) {
      Deeds.record(player, Deed.of(DeedKinds.ACCESSORIES_HELD).times((int) held));
    }
  }
}
