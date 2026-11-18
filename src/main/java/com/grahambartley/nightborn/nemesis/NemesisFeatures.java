package com.grahambartley.nightborn.nemesis;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class NemesisFeatures {

  static final int CHECK_TICKS = 20;

  private NemesisFeatures() {}

  public static void register() {
    NemesisKinds.register();
    NemesisAttachment.register();
    NemesisItems.register();
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (victim, source, base, taken, blocked) -> NemesisFights.afterDamage(victim, source));
    ServerLivingEntityEvents.AFTER_DEATH.register(NemesisEnding::afterDeath);
    ServerEntityEvents.ENTITY_LOAD.register(NemesisWatch::loaded);
    ServerEntityEvents.ENTITY_UNLOAD.register(NemesisWatch::unloaded);
    ServerTickEvents.END_SERVER_TICK.register(
        server -> {
          if (server.getTicks() % CHECK_TICKS == 0) {
            NemesisFights.tick(server);
            NemesisWatch.tick(server);
            NemesisVisits.tick(server);
          }
        });
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> {
          NemesisFights.clear();
          NemesisWatch.clear();
        });
  }
}
