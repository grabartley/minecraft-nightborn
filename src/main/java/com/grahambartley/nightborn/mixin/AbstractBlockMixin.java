package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.guild.party.CampClaims;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
abstract class AbstractBlockMixin {

  @Inject(method = "getDroppedStacks", at = @At("HEAD"), cancellable = true)
  private void nightborn$campBlocksDropNothing(
      final BlockState state,
      final LootContextParameterSet.Builder builder,
      final CallbackInfoReturnable<List<ItemStack>> cir) {
    final var origin = builder.getOptional(LootContextParameters.ORIGIN);
    if (origin != null
        && CampClaims.of(builder.getWorld()).release(BlockPos.ofFloored(origin), state)) {
      cir.setReturnValue(List.of());
    }
  }
}
