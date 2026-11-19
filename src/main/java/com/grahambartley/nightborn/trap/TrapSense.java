package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.sight.Sights;
import java.util.Map;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TrapSense {

  static final int COLOUR = 0xB8B8B0;
  static final double RANGE = 48.0;

  private TrapSense() {}

  static void register() {
    Sights.standing(TrapSense::show);
  }

  static void show(final ServerPlayerEntity player, final Map<Integer, Integer> shown) {
    if (!TrapAllies.hunter(player) || !TrapGifts.senses(HeldTrapGifts.of(player))) {
      return;
    }
    for (final TrapEntity trap :
        player
            .getServerWorld()
            .getEntitiesByClass(
                TrapEntity.class,
                player.getBoundingBox().expand(RANGE),
                candidate -> candidate.owner() != null)) {
      if (TrapAllies.allied(trap.owner(), player)) {
        shown.putIfAbsent(trap.getId(), COLOUR);
      }
    }
  }
}
