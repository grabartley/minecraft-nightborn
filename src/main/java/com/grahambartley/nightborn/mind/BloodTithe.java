package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.thrall.ThrallRegistry;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class BloodTithe {

  static final int TICKS_PER_SECOND = 20;

  static final AttachmentType<Double> PROGRESS =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "tithe_progress"));

  static final AttachmentType<Float> OWED =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "tithe_owed"));

  private static final ThreadLocal<Boolean> PASSING = ThreadLocal.withInitial(() -> false);

  private BloodTithe() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (vampire, source, base, taken, blocked) -> pass(vampire, source, taken));
  }

  public static float share(final LivingEntity vampire, final float amount) {
    vampire.removeAttached(OWED);
    final double share = MindAttributes.value(vampire, MindAttributes.TITHE_SHARE);
    if (share <= 0.0 || amount <= 0.0F || PASSING.get() || nearest(vampire).isEmpty()) {
      return amount;
    }
    vampire.setAttached(OWED, (float) TitheRules.clampShare(share));
    return TitheRules.kept(amount, share);
  }

  static void pass(final LivingEntity vampire, final DamageSource source, final float taken) {
    final Float share = vampire.getAttached(OWED);
    if (share == null) {
      return;
    }
    final float owed = TitheRules.owed(taken, share);
    vampire.removeAttached(OWED);
    nearest(vampire)
        .ifPresent(
            bearer -> {
              PASSING.set(true);
              try {
                bearer.damage(source, owed);
              } finally {
                PASSING.set(false);
              }
            });
  }

  public static void tick(final LivingEntity vampire) {
    final double rate = MindAttributes.value(vampire, MindAttributes.TITHE_RATE);
    if (rate <= 0.0 || vampire.age % TICKS_PER_SECOND != 0 || nearest(vampire).isEmpty()) {
      return;
    }
    final double progress = TitheRules.gathered(vampire.getAttachedOrElse(PROGRESS, 0.0), rate);
    if (progress >= 1.0) {
      BloodAttachment.set(vampire, BloodAttachment.get(vampire) + 1);
      vampire.setAttached(PROGRESS, progress - 1.0);
    } else {
      vampire.setAttached(PROGRESS, progress);
    }
  }

  static Optional<LivingEntity> nearest(final LivingEntity vampire) {
    if (!(vampire.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    final double range = MindAttributes.value(vampire, MindAttributes.TITHE_RANGE);
    return bound(world, vampire.getUuid())
        .filter(id -> !id.equals(vampire.getUuid()))
        .map(world::getEntity)
        .filter(entity -> entity instanceof LivingEntity living && living.isAlive())
        .map(LivingEntity.class::cast)
        .filter(living -> living.squaredDistanceTo(vampire) <= range * range)
        .filter(living -> !(living instanceof PlayerEntity) || PowerReach.pvp(vampire))
        .min(Comparator.comparingDouble(vampire::squaredDistanceTo));
  }

  private static Stream<UUID> bound(final ServerWorld world, final UUID vampire) {
    return Stream.concat(
        ThrallRegistry.of(world.getServer()).thralls(vampire).stream(),
        Houses.get(world.getServer())
            .of(vampire)
            .map(house -> house.everyone())
            .orElse(Stream.empty()));
  }
}
