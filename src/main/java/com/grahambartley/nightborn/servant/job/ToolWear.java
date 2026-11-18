package com.grahambartley.nightborn.servant.job;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class ToolWear {

  private ToolWear() {}

  public static void wear(final ServerWorld world, final Entity worker, final ItemStack tool) {
    if (tool.isEmpty()) {
      return;
    }
    tool.damage(
        1,
        world,
        null,
        item ->
            world.playSound(
                null,
                worker.getBlockPos(),
                SoundEvents.ENTITY_ITEM_BREAK,
                SoundCategory.NEUTRAL,
                0.8F,
                1.0F));
  }
}
