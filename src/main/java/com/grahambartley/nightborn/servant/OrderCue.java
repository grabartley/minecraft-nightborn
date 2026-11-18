package com.grahambartley.nightborn.servant;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class OrderCue {

  private OrderCue() {}

  public static void obey(final Entity servant, final PlayerEntity owner, final Text said) {
    give(servant, owner, said, SoundEvents.ENTITY_VILLAGER_YES, 0.7F);
  }

  public static void give(
      final Entity servant,
      final PlayerEntity owner,
      final Text said,
      final SoundEvent sound,
      final float pitch) {
    servant
        .getWorld()
        .playSound(null, servant.getBlockPos(), sound, SoundCategory.NEUTRAL, 0.8F, pitch);
    owner.sendMessage(said, true);
  }
}
