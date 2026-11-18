package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.name.StageNames;
import com.grahambartley.nightborn.network.ChooseTitlePayload;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class Titles {

  private Titles() {}

  static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        ChooseTitlePayload.ID, (payload, context) -> choose(context.player(), payload.title()));
  }

  public static void choose(final ServerPlayerEntity player, final Optional<Identifier> title) {
    final ChallengeRecord record = ChallengeAttachment.get(player);
    final FeatLog chosen = record.feats().wearing(title);
    if (chosen.title().equals(title) && !chosen.equals(record.feats())) {
      ChallengeAttachment.set(player, record.withFeats(chosen));
      StageNames.changed(player);
    }
  }

  public static Optional<Identifier> shown(
      final FeatLog feats,
      final boolean bearerVampire,
      final boolean viewerVampire,
      final boolean factionsShown) {
    return feats
        .title()
        .filter(
            title ->
                !feats.worn().orElse(false)
                    || StageNames.shown(
                            Optional.of(title), bearerVampire, viewerVampire, factionsShown)
                        .isPresent());
  }
}
