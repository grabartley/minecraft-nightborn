package com.grahambartley.nightborn.mob.brain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public final class HelpClaims {

  private static final Map<RegistryKey<World>, Claims> CLAIMS = new HashMap<>();

  private HelpClaims() {}

  static void register() {
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> CLAIMS.clear());
  }

  public static Claims in(final World world) {
    return CLAIMS.computeIfAbsent(world.getRegistryKey(), key -> new Claims());
  }

  public static void claim(
      final World world, final UUID ally, final UUID claimer, final long until) {
    in(world).claim(ally, claimer, until, world.getTime());
  }
}
