package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.decor.ChaliceBlock;
import com.grahambartley.nightborn.decor.ChaliceContents;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import com.grahambartley.nightborn.nemesis.Nemeses;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class ChaliceInvitation {

  static final String NOT_FULL = "nightborn.house.invite.chalice_not_full";
  static final String JOINED = "nightborn.house.invite.joined";

  private ChaliceInvitation() {}

  public static ActionResult onUse(
      final PlayerEntity player,
      final World world,
      final Hand hand,
      final Entity entity,
      @Nullable final EntityHitResult hit) {
    final ItemStack chalice = player.getStackInHand(hand);
    if (hand != Hand.MAIN_HAND
        || !(entity instanceof VampireEntity guest)
        || !chalice.isOf(DecorBlocks.CHALICE.asItem())) {
      return ActionResult.PASS;
    }
    if (!(player instanceof ServerPlayerEntity lord)) {
      return ActionResult.SUCCESS;
    }
    if (Houses.get(lord.getServer()).ledBy(lord.getUuid()).isEmpty()) {
      return ActionResult.PASS;
    }
    if (!ChaliceBlock.contents(chalice).full()) {
      lord.sendMessage(Text.translatable(NOT_FULL), true);
      return ActionResult.SUCCESS;
    }
    final HouseRules.Invite verdict = Lordship.judge(lord, guest);
    if (verdict != HouseRules.Invite.WELCOME) {
      lord.sendMessage(Text.translatable(verdict.key(), guest.getDisplayName()), true);
      return ActionResult.SUCCESS;
    }
    final Optional<BlockPos> formerHome =
        guest.householdTie().lord().isEmpty() ? guest.home() : Optional.empty();
    Houses.get(lord.getServer())
        .ledBy(lord.getUuid())
        .ifPresent(
            house -> {
              MemberLife.join(lord.getServerWorld(), guest, house);
              Nemeses.drewAway(lord.getServerWorld(), guest, formerHome, lord.getUuid());
              HouseInvites.guestArrived(lord, Houses.get(lord.getServer()));
            });
    if (!lord.getAbilities().creativeMode) {
      lord.setStackInHand(hand, ChaliceBlock.item(ChaliceContents.EMPTY));
    }
    world.playSound(
        null,
        guest.getBlockPos(),
        SoundEvents.ENTITY_GENERIC_DRINK,
        SoundCategory.NEUTRAL,
        1.0F,
        0.8F);
    guest.getLookControl().lookAt(lord);
    guest.speak(VampireVoice.TALK);
    lord.sendMessage(Text.translatable(JOINED, guest.getDisplayName()), false);
    return ActionResult.SUCCESS;
  }
}
