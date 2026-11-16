package com.grahambartley.nightborn.coffin;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public final class CoffinOwnership {

  private CoffinOwnership() {}

  public static Optional<UUID> owner(final World world, final BlockPos pos) {
    return world instanceof ServerWorld server
        ? head(server, pos).flatMap(head -> ownerAt(server, head))
        : Optional.empty();
  }

  public static Optional<UUID> ownerAt(final ServerWorld world, final BlockPos head) {
    return CoffinOwnersState.get(world.getServer()).ownerOf(at(world, head));
  }

  public static boolean claim(final ServerWorld world, final BlockPos head, final UUID owner) {
    return CoffinOwnersState.get(world.getServer()).assign(owner, at(world, head));
  }

  public static boolean respawnsAt(final ServerPlayerEntity sleeper, final BlockPos pos) {
    return head(sleeper.getServerWorld(), pos)
        .map(head -> rest(sleeper, head) == CoffinRest.RETURN)
        .orElse(true);
  }

  public static void rested(final ServerPlayerEntity sleeper, final BlockPos pos) {
    head(sleeper.getServerWorld(), pos)
        .ifPresent(
            head -> {
              switch (rest(sleeper, head)) {
                case CLAIM -> {
                  claim(sleeper.getServerWorld(), head, sleeper.getUuid());
                  CoffinRespawn.set(sleeper, head);
                }
                case BORROW -> sleeper.sendMessage(Text.translatable(RestRules.BORROWED), false);
                case RETURN -> {}
              }
            });
  }

  public static void release(final MinecraftServer server, final UUID owner) {
    CoffinOwnersState.get(server)
        .release(owner)
        .ifPresent(coffin -> CoffinRespawn.forget(server, owner, coffin));
  }

  public static void releaseAt(final ServerWorld world, final BlockPos head) {
    final GlobalPos coffin = at(world, head);
    CoffinOwnersState.get(world.getServer())
        .releaseCoffin(coffin)
        .ifPresent(owner -> CoffinRespawn.forget(world.getServer(), owner, coffin));
  }

  private static CoffinRest rest(final ServerPlayerEntity sleeper, final BlockPos head) {
    return CoffinRest.of(ownerAt(sleeper.getServerWorld(), head), sleeper.getUuid());
  }

  private static Optional<BlockPos> head(final ServerWorld world, final BlockPos pos) {
    final BlockState state = world.getBlockState(pos);
    return state.getBlock() instanceof CoffinBlock
        ? Optional.of(CoffinBlock.head(pos, state))
        : Optional.empty();
  }

  private static GlobalPos at(final ServerWorld world, final BlockPos head) {
    return GlobalPos.create(world.getRegistryKey(), head.toImmutable());
  }
}
