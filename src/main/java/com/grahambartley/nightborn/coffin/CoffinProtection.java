package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.server.ConfigEditPermission;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class CoffinProtection {

  public static final String OWNED = "nightborn.coffin.owned";

  private CoffinProtection() {}

  public static void register() {
    PlayerBlockBreakEvents.BEFORE.register(CoffinProtection::beforeBreak);
  }

  public static boolean shields(
      final World world, final BlockPos pos, @Nullable final Entity breaker) {
    if (!(world.getBlockState(pos).getBlock() instanceof CoffinBlock)) {
      return false;
    }
    final PlayerEntity player = breaker instanceof PlayerEntity p ? p : null;
    return !CoffinGuard.mayBreak(
        CoffinOwnership.owner(world, pos),
        breaker == null ? null : breaker.getUuid(),
        player != null && player.isCreative(),
        player != null && player.hasPermissionLevel(ConfigEditPermission.OPERATOR_LEVEL));
  }

  private static boolean beforeBreak(
      final World world,
      final PlayerEntity player,
      final BlockPos pos,
      final BlockState state,
      @Nullable final BlockEntity entity) {
    if (!shields(world, pos, player)) {
      return true;
    }
    player.sendMessage(Text.translatable(OWNED), true);
    resync(world, pos);
    return false;
  }

  public static void resync(final World world, final BlockPos pos) {
    final BlockState state = world.getBlockState(pos);
    final BlockPos head = CoffinBlock.head(pos, state);
    final BlockPos foot =
        CoffinLayout.foot(
            head, BedPart.HEAD, state.get(BedBlock.FACING), state.get(CoffinBlock.STANDING));
    for (final BlockPos part : new BlockPos[] {head, foot}) {
      final BlockState partState = world.getBlockState(part);
      world.updateListeners(part, partState, partState, Block.NOTIFY_ALL);
    }
  }
}
