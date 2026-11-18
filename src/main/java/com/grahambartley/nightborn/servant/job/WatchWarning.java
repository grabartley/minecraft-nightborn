package com.grahambartley.nightborn.servant.job;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class WatchWarning {

  private WatchWarning() {}

  public static boolean raise(final ServerWorld world, final DayWorker watch) {
    final ServerPlayerEntity owner =
        watch.owner().map(world.getServer().getPlayerManager()::getPlayer).orElse(null);
    if (owner == null || !(watch instanceof Entity entity)) {
      return false;
    }
    owner.sendMessage(Text.translatable("nightborn.watch.hunters", entity.getDisplayName()), false);
    owner.playSoundToPlayer(SoundEvents.BLOCK_BELL_USE, SoundCategory.NEUTRAL, 1.0F, 0.8F);
    return true;
  }
}
