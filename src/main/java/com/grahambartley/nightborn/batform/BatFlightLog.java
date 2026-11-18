package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class BatFlightLog {

  static final double MOST_PER_TICK = 4.0;
  static final int WHOLE_BLOCKS = 16;

  public record Flight(Vec3d at, long tick, double carried) {

    public Flight next(final Vec3d now, final long nowTick) {
      if (nowTick != tick + 1) {
        return new Flight(now, nowTick, carried);
      }
      final double moved = at.distanceTo(now);
      return new Flight(now, nowTick, moved > MOST_PER_TICK ? carried : carried + moved);
    }

    public int whole() {
      return carried >= WHOLE_BLOCKS ? (int) Math.floor(carried) : 0;
    }

    public Flight spent(final int blocks) {
      return new Flight(at, tick, carried - blocks);
    }
  }

  private static final Map<UUID, Flight> FLIGHTS = new HashMap<>();

  private BatFlightLog() {}

  static void tick(final ServerPlayerEntity player) {
    final long now = player.getServerWorld().getTime();
    final Flight before = FLIGHTS.get(player.getUuid());
    Flight flight =
        before == null ? new Flight(player.getPos(), now, 0.0) : before.next(player.getPos(), now);
    final int blocks = flight.whole();
    if (blocks > 0) {
      flight = flight.spent(blocks);
      Deeds.record(player, Deed.of(DeedKinds.BAT_FLOWN).times(blocks));
    }
    FLIGHTS.put(player.getUuid(), flight);
  }

  static void forget(final UUID player) {
    FLIGHTS.remove(player);
  }
}
