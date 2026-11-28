package com.grahambartley.nightborn.balance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class NightbornBalance implements ModInitializer {

  private BalanceRun run;

  @Override
  public void onInitialize() {
    ServerLifecycleEvents.SERVER_STARTED.register(
        server ->
            run =
                new BalanceRun(
                    server,
                    Matchup.chosen(System.getProperty("nightborn.balance.matchups")),
                    Integer.getInteger("nightborn.balance.rounds", 20)));
    ServerTickEvents.END_SERVER_TICK.register(
        server -> {
          if (run != null) {
            run.tick();
          }
        });
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, baseDamage, taken, blocked) -> {
          if (run != null) {
            run.afterDamage(entity, source, taken);
          }
        });
  }
}
