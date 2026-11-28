package com.grahambartley.nightborn.dismember;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class WoundSearing {

  public static final TagKey<Item> SEARS_WOUNDS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "sears_wounds"));

  private WoundSearing() {}

  public static void register() {
    UseItemCallback.EVENT.register(WoundSearing::sear);
  }

  public static TypedActionResult<ItemStack> sear(
      final PlayerEntity player, final World world, final Hand hand) {
    final ItemStack stack = player.getStackInHand(hand);
    if (!stack.isIn(SEARS_WOUNDS) || !Wounds.bleeding(player)) {
      return TypedActionResult.pass(stack);
    }
    if (Wounds.close(player)) {
      world.playSound(
          null,
          player.getBlockPos(),
          SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
          player.getSoundCategory(),
          1.0F,
          1.0F);
      final float loss = BleedRules.searLoss(player.getHealth());
      if (loss > 0.0F) {
        player.damage(NightbornDamage.searing(world), loss);
      }
      if (stack.isDamageable()) {
        stack.damage(1, player, LivingEntity.getSlotForHand(hand));
      }
    }
    player.swingHand(hand, true);
    return TypedActionResult.success(stack, world.isClient());
  }
}
