package com.grahambartley.nightborn.challenge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class TravelLog {

  static final double MOST_PER_TICK = 4.0;
  static final int WHOLE_BLOCKS = 16;

  public record Leg(Vec3d at, long tick, double carried) {

    public Leg next(final Vec3d now, final long nowTick) {
      if (nowTick != tick + 1) {
        return new Leg(now, nowTick, carried);
      }
      final double moved = at.distanceTo(now);
      return new Leg(now, nowTick, moved > MOST_PER_TICK ? carried : carried + moved);
    }

    public int whole() {
      return carried >= WHOLE_BLOCKS ? (int) Math.floor(carried) : 0;
    }

    public Leg spent(final int blocks) {
      return new Leg(at, tick, carried - blocks);
    }
  }

  private final Identifier deed;
  private final Map<UUID, Leg> legs = new HashMap<>();

  public TravelLog(final Identifier deed) {
    this.deed = deed;
  }

  public int step(final ServerPlayerEntity player) {
    final long now = player.getServerWorld().getTime();
    final Leg before = legs.get(player.getUuid());
    Leg leg =
        before == null ? new Leg(player.getPos(), now, 0.0) : before.next(player.getPos(), now);
    final int blocks = leg.whole();
    if (blocks > 0) {
      leg = leg.spent(blocks);
      Deeds.record(player, Deed.of(deed).times(blocks));
    }
    legs.put(player.getUuid(), leg);
    return blocks;
  }

  public void forget(final UUID player) {
    legs.remove(player);
  }
}
