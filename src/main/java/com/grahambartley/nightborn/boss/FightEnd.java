package com.grahambartley.nightborn.boss;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

public final class FightEnd {

  private FightEnd() {}

  public static void register() {
    ServerEntityEvents.ENTITY_UNLOAD.register(
        (entity, world) -> {
          if (entity instanceof MobEntity
              && entity instanceof ArenaFighter
              && ends(entity.getRemovalReason())) {
            end((MobEntity & ArenaFighter) entity);
          }
        });
  }

  public static boolean ends(@Nullable final Entity.RemovalReason reason) {
    return reason != null
        && switch (reason) {
          case KILLED, DISCARDED, CHANGED_DIMENSION -> true;
          case UNLOADED_TO_CHUNK, UNLOADED_WITH_PLAYER -> false;
        };
  }

  private static <T extends MobEntity & ArenaFighter> void end(final T boss) {
    boss.bossFight().end(boss);
  }
}
