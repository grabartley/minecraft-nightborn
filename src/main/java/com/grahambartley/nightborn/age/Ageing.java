package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.challenge.NightDeeds;
import com.grahambartley.nightborn.config.AgeSettings;
import com.grahambartley.nightborn.mob.stage.StageDeed;
import com.grahambartley.nightborn.name.StageNames;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class Ageing {

  private Ageing() {}

  public static void tick(final ServerPlayerEntity vampire) {
    final long now = vampire.getServerWorld().getTimeOfDay();
    final Long previous = vampire.getAttached(AgeAttachment.CLOCK);
    vampire.setAttached(AgeAttachment.CLOCK, now);
    final AgeSettings settings = ServerConfigService.current().age();
    if (previous != null) {
      final NightWatch watch = vampire.getAttachedOrElse(AgeAttachment.WATCH, NightWatch.NONE);
      final AgeNight.Step step = AgeNight.step(watch, previous, now);
      if (!step.watch().equals(watch)) {
        vampire.setAttached(AgeAttachment.WATCH, step.watch());
      }
      if (step.nightsLived() > 0) {
        settle(vampire, AgeAttachment.get(vampire).lived(step.nightsLived()), settings);
        NightDeeds.lived(vampire, step.nightsLived());
      }
    }
    AgeBody.apply(vampire, settings);
  }

  public static void drank(final ServerPlayerEntity vampire, final int points) {
    if (points > 0 && Vampires.isVampire(vampire)) {
      settle(
          vampire, AgeAttachment.get(vampire).drank(points), ServerConfigService.current().age());
    }
  }

  public static void did(
      final ServerPlayerEntity vampire, final StageDeed.Act act, final EntityType<?> victim) {
    if (!Vampires.isVampire(vampire)) {
      return;
    }
    final AgeLadder ladder = AgeLadder.of(vampire.getRegistryManager());
    AgeProgress progress = AgeAttachment.get(vampire);
    for (int rank = 0; rank < ladder.size(); rank++) {
      final boolean done =
          ladder
              .rungs()
              .get(rank)
              .age()
              .deed()
              .map(deed -> deed.doneBy(act, victim.isIn(deed.victims())))
              .orElse(false);
      if (done) {
        progress = progress.withDeed(rank);
      }
    }
    settle(vampire, progress, ServerConfigService.current().age());
  }

  public static boolean setRank(final ServerPlayerEntity vampire, final int rank) {
    final AgeProgress progress = AgeAttachment.get(vampire);
    if (!Vampires.isVampire(vampire) || progress.rank() == rank) {
      return false;
    }
    AgeAttachment.set(vampire, progress.withRank(rank));
    changed(vampire);
    return true;
  }

  public static void clear(final ServerPlayerEntity player) {
    AgeAttachment.clear(player);
    AgeBody.remove(player);
    StageNames.changed(player);
  }

  private static void settle(
      final ServerPlayerEntity vampire, final AgeProgress progress, final AgeSettings settings) {
    final AgeLadder ladder = AgeLadder.of(vampire.getRegistryManager());
    final AgeProgress settled = AgeRules.advance(progress, ladder.size(), settings.needs());
    final int before = AgeAttachment.get(vampire).rank();
    AgeAttachment.set(vampire, settled);
    if (settled.rank() > before) {
      changed(vampire);
      for (int rank = before + 1; rank <= settled.rank(); rank++) {
        final boolean top = ladder.top(rank);
        ladder
            .rung(rank)
            .ifPresent(
                rung -> {
                  herald(vampire, rung);
                  Deeds.record(
                      vampire,
                      Deed.of(DeedKinds.STAGE_REACHED)
                          .with(DeedKinds.STAGE, rung.id())
                          .with(DeedKinds.TOP, top));
                });
      }
    }
  }

  private static void changed(final ServerPlayerEntity vampire) {
    AgeBody.apply(vampire, ServerConfigService.current().age());
    StageNames.announce(vampire);
  }

  private static void herald(final ServerPlayerEntity vampire, final AgeLadder.Rung rung) {
    vampire.sendMessage(Text.translatable("nightborn.age.reached", rung.age().name()), false);
    vampire
        .getServerWorld()
        .playSound(
            null,
            vampire.getBlockPos(),
            SoundEvents.BLOCK_BELL_RESONATE,
            SoundCategory.PLAYERS,
            1.0F,
            0.5F);
  }
}
