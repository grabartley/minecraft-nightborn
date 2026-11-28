package com.grahambartley.nightborn.mob.combat;

import java.util.function.Predicate;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Box;

public final class PresenceAura {

  private PresenceAura() {}

  public static void spread(
      final MobEntity leader, final Presence presence, final Predicate<MobEntity> follows) {
    if (!Presence.pulses(leader.age)) {
      return;
    }
    final boolean leading = leader.getTarget() != null;
    Registries.STATUS_EFFECT
        .getEntry(presence.effect())
        .ifPresent(
            effect ->
                leader
                    .getWorld()
                    .getEntitiesByClass(
                        MobEntity.class,
                        new Box(leader.getBlockPos()).expand(presence.radius()),
                        ally ->
                            ally != leader
                                && ally.isAlive()
                                && follows.test(ally)
                                && presence.reaches(
                                    leading, ally.getTarget() != null, leader.distanceTo(ally)))
                    .forEach(
                        ally ->
                            ally.addStatusEffect(
                                new StatusEffectInstance(
                                    effect,
                                    Presence.LINGER_TICKS,
                                    presence.amplifier(),
                                    true,
                                    false,
                                    true),
                                leader)));
  }
}
