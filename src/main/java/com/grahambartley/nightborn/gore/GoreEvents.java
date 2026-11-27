package com.grahambartley.nightborn.gore;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.dismember.Dismemberment;
import com.grahambartley.nightborn.network.GorePayload;
import com.grahambartley.nightborn.network.TrackingSends;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public final class GoreEvents {

  private GoreEvents() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> {
          if (!blocked && taken > 0.0F) {
            final Blow blow = Blows.read(source);
            send(entity, source, onHit(entity, blow));
            Dismemberment.onHit(entity, source, blow, taken);
          }
        });
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (entity, source) -> send(entity, source, onDeath(entity, Blows.read(source))));
  }

  public static Optional<Gore> onHit(final LivingEntity entity, final DamageSource source) {
    return onHit(entity, Blows.read(source));
  }

  public static Optional<Gore> onDeath(final LivingEntity entity, final DamageSource source) {
    return onDeath(entity, Blows.read(source));
  }

  private static Optional<Gore> onHit(final LivingEntity entity, final Blow blow) {
    return GoreRules.onHit(bleeds(entity), Vampires.isVampire(entity), blow);
  }

  private static Optional<Gore> onDeath(final LivingEntity entity, final Blow blow) {
    return GoreRules.onDeath(bleeds(entity), Vampires.isVampire(entity), blow);
  }

  private static boolean bleeds(final LivingEntity entity) {
    return Vampires.isVampire(entity) || BloodQuality.of(entity.getType()).isPresent();
  }

  private static void send(
      final LivingEntity entity, final DamageSource source, final Optional<Gore> gore) {
    if (gore.isEmpty()) {
      return;
    }
    final Entity from = source.getSource() != null ? source.getSource() : source.getAttacker();
    final float yaw =
        from == null
            ? Float.NaN
            : GoreRules.sprayYaw(entity.getX() - from.getX(), entity.getZ() - from.getZ());
    TrackingSends.toSelfAndWatchers(entity, new GorePayload(entity.getId(), gore.get(), yaw));
  }
}
