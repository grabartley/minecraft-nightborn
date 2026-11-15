package com.grahambartley.nightborn.coffin;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BedBlock;
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

  public static Optional<GlobalPos> coffinOf(final MinecraftServer server, final UUID owner) {
    return CoffinOwnersState.get(server).coffinOf(owner);
  }

  public static boolean claim(final ServerWorld world, final BlockPos head, final UUID owner) {
    return CoffinOwnersState.get(world.getServer()).assign(owner, at(world, head));
  }

  public static boolean rest(final ServerPlayerEntity sleeper, final BlockPos pos) {
    final ServerWorld world = sleeper.getServerWorld();
    final Optional<BlockPos> head = head(world, pos);
    if (head.isEmpty()) {
      return true;
    }
    final CoffinRest rest = CoffinRest.of(ownerAt(world, head.get()), sleeper.getUuid());
    switch (rest) {
      case CLAIM -> claim(world, head.get(), sleeper.getUuid());
      case BORROW -> sleeper.sendMessage(Text.translatable(RestRules.BORROWED), true);
      case RETURN -> {}
    }
    return rest.setsRespawn();
  }

  public static void release(final MinecraftServer server, final UUID owner) {
    CoffinOwnersState.get(server)
        .release(owner)
        .ifPresent(coffin -> forgetRespawn(server, owner, coffin));
  }

  public static void releaseAt(final ServerWorld world, final BlockPos head) {
    final GlobalPos coffin = at(world, head);
    CoffinOwnersState.get(world.getServer())
        .releaseCoffin(coffin)
        .ifPresent(owner -> forgetRespawn(world.getServer(), owner, coffin));
  }

  private static void forgetRespawn(
      final MinecraftServer server, final UUID owner, final GlobalPos coffin) {
    final ServerPlayerEntity player = server.getPlayerManager().getPlayer(owner);
    if (player != null
        && coffin.pos().equals(player.getSpawnPointPosition())
        && coffin.dimension().equals(player.getSpawnPointDimension())) {
      player.setSpawnPoint(World.OVERWORLD, null, 0.0F, false, false);
    }
  }

  private static Optional<BlockPos> head(final ServerWorld world, final BlockPos pos) {
    final BlockState state = world.getBlockState(pos);
    if (!(state.getBlock() instanceof CoffinBlock)) {
      return Optional.empty();
    }
    return Optional.of(
        CoffinLayout.head(
            pos,
            state.get(BedBlock.PART),
            state.get(BedBlock.FACING),
            state.get(CoffinBlock.STANDING)));
  }

  private static GlobalPos at(final ServerWorld world, final BlockPos head) {
    return GlobalPos.create(world.getRegistryKey(), head.toImmutable());
  }
}
