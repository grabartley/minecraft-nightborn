package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public final class ChaliceUse {

  private ChaliceUse() {}

  public static ChalicePour.Held held(final ItemStack stack) {
    if (stack.isEmpty()) {
      return ChalicePour.Held.EMPTY_HAND;
    }
    if (stack.isOf(BloodItems.BLOOD_BOTTLE)) {
      return ChalicePour.Held.BLOOD_BOTTLE;
    }
    return stack.isOf(Items.GLASS_BOTTLE) ? ChalicePour.Held.GLASS_BOTTLE : ChalicePour.Held.OTHER;
  }

  public static Optional<ChalicePour> decide(
      final ChaliceContents contents, final ItemStack stack, final PlayerEntity player) {
    final ChalicePour.Held held = held(stack);
    return ChalicePour.decide(
        contents,
        held,
        held == ChalicePour.Held.BLOOD_BOTTLE
            ? Optional.of(BloodBottleItem.qualityOf(stack))
            : Optional.empty(),
        Vampires.isVampire(player));
  }

  public static ChaliceContents apply(
      final ChalicePour pour,
      final ChaliceContents contents,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand) {
    final ItemStack stack = player.getStackInHand(hand);
    return switch (pour) {
      case FILL -> {
        final BloodQuality quality = BloodBottleItem.qualityOf(stack);
        player.setStackInHand(
            hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
        final ChaliceContents poured = contents.poured(quality);
        if (poured.full() && !contents.full()) {
          Deeds.record(player, Deed.of(DeedKinds.CHALICE_FILLED));
        }
        yield changed(world, pos, player, poured, SoundEvents.ITEM_BOTTLE_EMPTY);
      }
      case BOTTLE -> {
        player.setStackInHand(
            hand,
            ItemUsage.exchangeStack(
                stack, player, BloodBottleItem.of(contents.quality().orElseThrow())));
        yield changed(world, pos, player, contents.less(), SoundEvents.ITEM_BOTTLE_FILL);
      }
      case DRINK -> {
        BloodBottleItem.drink(player, contents.quality().orElseThrow());
        yield changed(world, pos, player, contents.less(), SoundEvents.ENTITY_GENERIC_DRINK);
      }
      case WRONG_QUALITY, BRIMMING -> {
        pour.refusal().ifPresent(key -> player.sendMessage(Text.translatable(key), true));
        yield contents;
      }
    };
  }

  private static ChaliceContents changed(
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final ChaliceContents contents,
      final SoundEvent sound) {
    world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
    world.emitGameEvent(
        player, contents.empty() ? GameEvent.FLUID_PICKUP : GameEvent.FLUID_PLACE, pos);
    return contents;
  }
}
