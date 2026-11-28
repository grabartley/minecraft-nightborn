package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.faction.Allies;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Box;

public final class PresenceAura {

  private PresenceAura() {}

  public static void spread(final MobEntity leader, final Presence presence) {
    if (!Presence.pulses(leader.age) || leader.getTarget() == null) {
      return;
    }
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
                                && Allies.allied(leader, ally)
                                && presence.reaches(
                                    true, ally.getTarget() != null, leader.distanceTo(ally)))
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
