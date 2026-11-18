package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.mind.MindTies;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class TradeRefusal {

  static final int HEAD_SHAKE_TICKS = 40;

  private TradeRefusal() {}

  static ActionResult onUse(
      final PlayerEntity player,
      final World world,
      final Hand hand,
      final Entity entity,
      @Nullable final EntityHitResult hit) {
    if (world.isClient
        || !(entity instanceof VillagerEntity villager)
        || !KnownFaces.shuns(
            VillageSuspicion.knows(villager, player), MindTies.charmedBy(villager, player))) {
      return ActionResult.PASS;
    }
    if (hand == Hand.MAIN_HAND) {
      villager.setHeadRollingTimeLeft(HEAD_SHAKE_TICKS);
      villager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, villager.getSoundPitch());
    }
    return ActionResult.FAIL;
  }
}
