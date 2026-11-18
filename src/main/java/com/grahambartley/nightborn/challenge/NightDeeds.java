package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.house.House;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.house.Lordship;
import com.grahambartley.nightborn.thrall.ThrallRegistry;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;

public final class NightDeeds {

  private NightDeeds() {}

  public static void lived(final ServerPlayerEntity vampire, final int nights) {
    final MinecraftServer server = vampire.getServer();
    if (server == null || nights <= 0) {
      return;
    }
    final Optional<House> house = Houses.get(server).ledBy(vampire.getUuid());
    final boolean full = house.filter(h -> h.size() >= Lordship.capacity(vampire)).isPresent();
    final Deed deed =
        Deed.of(DeedKinds.NIGHT_LIVED)
            .with(DeedKinds.THRALL, ThrallRegistry.of(server).count(vampire.getUuid()) > 0)
            .with(DeedKinds.HOUSE_FULL, full)
            .with(
                DeedKinds.CASTLE_FULL,
                full
                    && house
                        .flatMap(House::seat)
                        .filter(seat -> inCastle(server, seat))
                        .isPresent());
    for (int night = 0; night < nights; night++) {
      Deeds.record(vampire, deed);
    }
  }

  private static boolean inCastle(final MinecraftServer server, final GlobalPos seat) {
    final ServerWorld world = server.getWorld(seat.dimension());
    return world != null && Castles.contains(world, seat.pos());
  }
}
