package com.grahambartley.nightborn.name;

import com.grahambartley.nightborn.network.NameServantPayload;
import com.grahambartley.nightborn.network.OpenNamingPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ServantNaming {

  private ServantNaming() {}

  static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        NameServantPayload.ID,
        (payload, context) -> rename(context.player(), payload.entityId(), payload.name()));
  }

  public static void offer(final ServerPlayerEntity owner, final MobEntity servant) {
    MobNaming.suggestion(owner.getServerWorld())
        .ifPresent(
            name -> {
              MobNaming.give(servant, name);
              ServerPlayNetworking.send(owner, new OpenNamingPayload(servant.getId(), name));
            });
  }

  public static boolean rename(
      final ServerPlayerEntity player, final int entityId, final String typed) {
    final Entity entity = player.getServerWorld().getEntityById(entityId);
    if (!(entity instanceof MobEntity mob)
        || !(entity instanceof NameBearer bearer)
        || !NamingRights.mayName(bearer.namedBy(), player.getUuid(), mob.isAlive())) {
      return false;
    }
    final NameRules.Verdict verdict = NameRules.check(typed);
    verdict.name().ifPresent(name -> MobNaming.give(mob, name));
    return verdict.ok();
  }
}
