package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.decor.FlameGesture;
import com.grahambartley.nightborn.mob.household.FurnitureUse;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

  @WrapMethod(method = "interactBlock")
  private ActionResult nightborn$gestureAtFlame(
      final ServerPlayerEntity player,
      final World world,
      final ItemStack stack,
      final Hand hand,
      final BlockHitResult hit,
      final Operation<ActionResult> original) {
    final boolean litBefore = FurnitureUse.lit(world.getBlockState(hit.getBlockPos()));
    final ActionResult result = original.call(player, world, stack, hand, hit);
    FlameGesture.afterUse(player, world, hit.getBlockPos(), litBefore);
    return result;
  }
}
