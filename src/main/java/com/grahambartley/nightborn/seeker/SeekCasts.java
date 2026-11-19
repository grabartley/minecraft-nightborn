package com.grahambartley.nightborn.seeker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public final class SeekCasts {

  private SeekCasts() {}

  public static void apply(
      final LivingEntity caster, final PowerSeek seek, @Nullable final LivingEntity target) {
    if (caster instanceof ServerPlayerEntity hunter) {
      seek.readScene()
          .flatMap(scene -> Scenes.read(hunter, scene))
          .ifPresent(scene -> SceneReport.tell(hunter, scene));
    }
    if (target != null) {
      seek.markQuarry().ifPresent(mark -> Quarries.mark(caster, target, mark));
    }
    seek.trueSight().ifPresent(sight -> TrueSights.see(caster, sight));
    seek.revelation().ifPresent(reveal -> Revelations.reveal(caster, reveal));
    if (seek.count() > 0) {
      caster.swingHand(Hand.MAIN_HAND, true);
    }
  }

  public static boolean sceneless(final LivingEntity caster, final PowerSeek seek) {
    return seek.readScene().map(scene -> Scenes.read(caster, scene).isEmpty()).orElse(false);
  }
}
