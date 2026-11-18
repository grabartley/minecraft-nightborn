package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.Housemates;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class HouseDefence {

  private HouseDefence() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, baseDamage, damage, blocked) -> defend(entity, source));
  }

  static void defend(final LivingEntity hurt, final DamageSource source) {
    if (!(hurt instanceof ServerPlayerEntity player)
        || !(source.getAttacker() instanceof LivingEntity attacker)
        || attacker == hurt) {
      return;
    }
    final int range = ServerConfigService.current().mobs().vampireAlertRange();
    final boolean attackerIsVampire = Vampires.isVampire(attacker);
    Houses.get(player.getServer())
        .of(player.getUuid())
        .ifPresent(
            house ->
                player
                    .getServerWorld()
                    .getEntitiesByClass(
                        VampireEntity.class,
                        player.getBoundingBox().expand(range),
                        member -> member.isAlive() && house.npcs().contains(member.getUuid()))
                    .stream()
                    .filter(
                        member ->
                            !member.isSleeping()
                                && Housemates.alerted(
                                    true,
                                    attackerIsVampire,
                                    member.squaredDistanceTo(player),
                                    range))
                    .forEach(member -> member.setAttacker(attacker)));
  }
}
