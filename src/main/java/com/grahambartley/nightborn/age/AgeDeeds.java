package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.mob.stage.StageDeed;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class AgeDeeds {

  private AgeDeeds() {}

  public static void register() {
    AgeAttachment.register();
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (victim, source) -> {
          if (source.getAttacker() instanceof ServerPlayerEntity killer) {
            Ageing.did(killer, StageDeed.Act.END, victim.getType());
          }
        });
  }

  public static void drained(final ServerPlayerEntity vampire, final LivingEntity victim) {
    Ageing.did(vampire, StageDeed.Act.DRAIN, victim.getType());
  }
}
