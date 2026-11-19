package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.house.House;
import com.grahambartley.nightborn.house.Houses;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class HouseRally {

  private HouseRally() {}

  public static double factor(final LivingEntity striker) {
    if (striker instanceof PlayerEntity || !(striker.getWorld() instanceof ServerWorld world)) {
      return 1.0;
    }
    final MinecraftServer server = world.getServer();
    final UUID self = striker.getUuid();
    final Optional<House> house =
        Houses.get(server).of(self).filter(found -> found.npcs().contains(self));
    if (house.isEmpty()) {
      return 1.0;
    }
    double best = 1.0;
    for (final UUID member : house.get().everyone().toList()) {
      final ServerPlayerEntity wearer = server.getPlayerManager().getPlayer(member);
      if (wearer != null && wearer.getWorld() == world) {
        best =
            Math.max(
                best,
                rally(
                    wearer.squaredDistanceTo(striker),
                    GiftAttributes.value(wearer, GiftAttributes.HOUSE_DAMAGE_RANGE),
                    GiftAttributes.value(wearer, GiftAttributes.HOUSE_DAMAGE)));
      }
    }
    return best;
  }

  public static double rally(final double squaredDistance, final double range, final double boost) {
    return range > 0.0 && squaredDistance <= range * range ? Math.max(1.0, boost) : 1.0;
  }
}
