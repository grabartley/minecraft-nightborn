package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.hunt.HuntAttachment;
import com.grahambartley.nightborn.hunt.Hunts;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class InquisitorDeaths {

  static final double SEARCH = 64.0;

  private InquisitorDeaths() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(InquisitorDeaths::afterDeath);
  }

  public static double kept(final double notoriety, final int lossPercent) {
    return Math.max(0.0, notoriety * (100 - Math.max(0, Math.min(100, lossPercent))) / 100.0);
  }

  private static void afterDeath(final LivingEntity dead, final DamageSource source) {
    if (!(dead instanceof ServerPlayerEntity player)) {
      return;
    }
    final List<GrandInquisitorEntity> fights =
        player
            .getServerWorld()
            .getEntitiesByClass(
                GrandInquisitorEntity.class,
                player.getBoundingBox().expand(SEARCH),
                inquisitor -> inquisitor.fellIn(player));
    if (fights.isEmpty()) {
      return;
    }
    fights.forEach(inquisitor -> inquisitor.fallen(player));
    if (Vampires.isVampire(player)) {
      Hunts.set(
          player,
          kept(
              HuntAttachment.get(player).notoriety(),
              ServerConfigService.current().inquisitor().deathNotorietyLoss()));
    }
  }
}
