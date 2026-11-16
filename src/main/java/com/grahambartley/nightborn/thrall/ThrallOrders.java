package com.grahambartley.nightborn.thrall;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class ThrallOrders {

  private ThrallOrders() {}

  public static boolean takesOrder(
      final boolean owner,
      final boolean mainHand,
      final boolean emptyHand,
      final boolean sneaking) {
    return owner && mainHand && emptyHand && !sneaking;
  }

  static ActionResult interact(
      final ThrallEntity thrall, final PlayerEntity player, final Hand hand) {
    if (!takesOrder(
        thrall.isOwnedBy(player),
        hand == Hand.MAIN_HAND,
        player.getStackInHand(hand).isEmpty(),
        player.isSneaking())) {
      return ActionResult.PASS;
    }
    final ThrallMode next = thrall.mode().next();
    thrall.order(next);
    thrall
        .getWorld()
        .playSound(
            null,
            thrall.getBlockPos(),
            SoundEvents.ENTITY_VILLAGER_YES,
            SoundCategory.NEUTRAL,
            0.8F,
            0.7F);
    player.sendMessage(Text.translatable(next.translationKey(), thrall.getDisplayName()), true);
    return ActionResult.SUCCESS;
  }
}
