package com.grahambartley.nightborn.servant.job;

import com.grahambartley.nightborn.servant.ChestMarking;
import com.grahambartley.nightborn.servant.ServantChests;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public final class JobChest {

  private JobChest() {}

  public static Optional<BlockPos> near(
      final ServerWorld world, final UUID owner, final BlockPos place) {
    final int range = ServerConfigService.current().familiars().errandRange();
    return ServantChests.of(world.getServer())
        .marksOf(owner)
        .workChest()
        .filter(chest -> chest.dimension() == world.getRegistryKey())
        .map(GlobalPos::pos)
        .filter(chest -> inReach(place, chest, range))
        .filter(chest -> ChestMarking.isErrandChest(world, chest));
  }

  public static boolean inReach(final BlockPos place, final BlockPos chest, final int range) {
    return place.getSquaredDistance(chest) <= (double) range * range;
  }
}
