package com.grahambartley.nightborn.client.sight;

import com.grahambartley.nightborn.network.RevealPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;

public final class RevealView {

  private record Shown(int colour, long until) {}

  private static final Map<Integer, Shown> SHOWN = new HashMap<>();

  private RevealView() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        RevealPayload.ID,
        (payload, context) -> {
          final long until =
              context.client().world == null
                  ? 0L
                  : context.client().world.getTime() + payload.ticks();
          SHOWN.clear();
          for (int i = 0; i < payload.entityIds().size(); i++) {
            SHOWN.put(payload.entityIds().get(i), new Shown(payload.colours().get(i), until));
          }
        });
  }

  public static OptionalInt colour(final Entity entity) {
    final Shown shown = SHOWN.get(entity.getId());
    if (shown == null || entity.getWorld().getTime() >= shown.until()) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(shown.colour());
  }
}
