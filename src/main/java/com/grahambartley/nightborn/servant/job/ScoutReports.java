package com.grahambartley.nightborn.servant.job;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class ScoutReports {

  private ScoutReports() {}

  public static boolean tell(final ServerWorld world, final DayWorker scout, final ScoutTrip trip) {
    final ServerPlayerEntity owner =
        scout.owner().map(world.getServer().getPlayerManager()::getPlayer).orElse(null);
    if (owner == null || !(scout instanceof Entity entity)) {
      return false;
    }
    owner.sendMessage(text(entity.getDisplayName(), trip), false);
    owner.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.NEUTRAL, 0.8F, 0.8F);
    return true;
  }

  public static Text text(final Text scout, final ScoutTrip trip) {
    final Text place = trip.placeName();
    return trip.report()
        .<Text>map(
            seen ->
                Text.translatable(
                    "nightborn.scout.report",
                    scout,
                    place,
                    seen.villagers(),
                    seen.hunters(),
                    seen.vampires(),
                    seen.humans()))
        .orElseGet(() -> Text.translatable("nightborn.scout.lost", scout, place));
  }
}
