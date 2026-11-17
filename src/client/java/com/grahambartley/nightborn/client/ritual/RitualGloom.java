package com.grahambartley.nightborn.client.ritual;

import com.grahambartley.nightborn.network.RitualGloomPayload;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class RitualGloom {

  private record Rite(RitualGloomPayload gloom, long began, RegistryKey<World> world) {}

  private static Optional<Rite> rite = Optional.empty();

  private RitualGloom() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        RitualGloomPayload.ID,
        (payload, context) -> {
          final World world = context.player().getWorld();
          rite =
              payload.lifts()
                  ? Optional.empty()
                  : Optional.of(new Rite(payload, world.getTime(), world.getRegistryKey()));
        });
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> rite = Optional.empty());
  }

  public static float strength(final ClientPlayerEntity player) {
    if (player == null || rite.isEmpty()) {
      return 0.0F;
    }
    final Rite current = rite.get();
    if (!current.world().equals(player.getWorld().getRegistryKey())) {
      rite = Optional.empty();
      return 0.0F;
    }
    final float strength =
        GloomCurve.strength(
            player.getWorld().getTime() - current.began(), current.gloom().buildUpTicks());
    if (strength <= 0.0F) {
      rite = Optional.empty();
      return 0.0F;
    }
    return strength
        * GloomCurve.reach(
            Math.sqrt(player.squaredDistanceTo(Vec3d.ofCenter(current.gloom().altar()))),
            current.gloom().range());
  }
}
