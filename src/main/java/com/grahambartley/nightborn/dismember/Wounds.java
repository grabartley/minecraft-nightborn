package com.grahambartley.nightborn.dismember;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;

public final class Wounds {

  private Wounds() {}

  public static boolean bleeding(final LivingEntity entity) {
    return Dismemberment.state(entity).bleeding(entity.getWorld().getTime());
  }

  public static void tick(final LivingEntity entity) {
    final BodyState body = entity.getAttached(Dismemberment.TYPE);
    if (body == null || !entity.isAlive()) {
      return;
    }
    final long now = entity.getWorld().getTime();
    if (!body.bleeding(now)
        || !BleedRules.pulses(
            body.lastCutAt(),
            now,
            ServerConfigService.current().dismemberment().bleedPulseSeconds())) {
      return;
    }
    if (Vampires.isVampire(entity)) {
      BloodAttachment.set(entity, BleedRules.bloodLeft(BloodAttachment.get(entity)));
      return;
    }
    final float loss = BleedRules.healthLoss(entity.getHealth());
    if (loss > 0.0F) {
      entity.damage(NightbornDamage.bleeding(entity.getWorld()), loss);
    }
  }

  public static boolean close(final LivingEntity entity) {
    final BodyState body = Dismemberment.state(entity);
    final long now = entity.getWorld().getTime();
    if (entity.getWorld().isClient() || !body.bleeding(now)) {
      return false;
    }
    entity.setAttached(Dismemberment.TYPE, body.closeWounds(now));
    return true;
  }
}
