package com.grahambartley.nightborn.village;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BellBlock;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;

public final class VillageWatch {

  static final int CHECK_TICKS = 100;

  private VillageWatch() {}

  static void register() {
    ServerTickEvents.END_WORLD_TICK.register(VillageWatch::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> VillageDoors.clear());
  }

  private static void tick(final ServerWorld world) {
    if (world.getTime() % CHECK_TICKS != 0) {
      return;
    }
    final Map<BlockPos, VillageRecord> records = Villages.of(world).all();
    for (final BlockPos bell : records.keySet()) {
      if (world.isChunkLoaded(
          ChunkSectionPos.getSectionCoord(bell.getX()),
          ChunkSectionPos.getSectionCoord(bell.getZ()))) {
        watch(world, bell);
      }
    }
  }

  static void watch(final ServerWorld world, final BlockPos bell) {
    if (!(world.getBlockState(bell).getBlock() instanceof BellBlock)) {
      VillageWards.takeDown(world, Villages.of(world).at(bell).orElseThrow());
      Villages.of(world).forget(bell);
      return;
    }
    VillageRecord record = VillageSuspicion.current(world, bell);
    final VillageStep step = VillageStep.of(record.suspicion(), VillageSuspicion.settings());
    final List<VillagerEntity> villagers =
        world.getEntitiesByClass(
            VillagerEntity.class,
            new Box(bell).expand(VillageSuspicion.settings().villageReach()),
            villager -> VillageSuspicion.bellOf(villager).equals(Optional.of(bell)));
    VillageDoors.keep(world, bell, step, villagers);
    record = VillageWards.keep(world, bell, step, record, villagers);
    record = VillageGuard.keep(world, bell, step, record);
    KnownFaces.shun(world, bell, record);
    Villages.of(world).put(bell, record);
  }
}
