package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.server.ConfigEditPermission;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
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
    if (player instanceof ServerPlayerEntity server) {
      server.networkHandler.sendPacket(
          new BlockUpdateS2CPacket(
              world,
              pos.offset(
                  CoffinLayout.towardsOtherPart(
                      state.get(BedBlock.PART),
                      state.get(BedBlock.FACING),
                      state.get(CoffinBlock.STANDING)))));
    }
    return false;
  }
}
