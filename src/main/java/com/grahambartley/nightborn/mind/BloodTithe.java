package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.thrall.ThrallRegistry;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class BloodTithe {

  static final int TICKS_PER_SECOND = 20;

  static final AttachmentType<Double> PROGRESS =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "tithe_progress"));

  private static final ThreadLocal<Boolean> PASSING = ThreadLocal.withInitial(() -> false);

  private BloodTithe() {}

  public static float share(
      final LivingEntity vampire, final DamageSource source, final float amount) {
    final double share = MindAttributes.value(vampire, MindAttributes.TITHE_SHARE);
    if (share <= 0.0 || amount <= 0.0F || PASSING.get()) {
      return amount;
    }
    final Optional<LivingEntity> bearer = nearest(vampire);
    if (bearer.isEmpty()) {
      return amount;
    }
    PASSING.set(true);
    try {
      bearer.get().damage(source, TitheRules.passed(amount, share));
    } finally {
      PASSING.set(false);
    }
    return TitheRules.kept(amount, share);
  }

  public static void tick(final LivingEntity vampire) {
    final double rate = MindAttributes.value(vampire, MindAttributes.TITHE_RATE);
    if (rate <= 0.0 || vampire.age % TICKS_PER_SECOND != 0) {
      return;
    }
    if (nearest(vampire).isEmpty()) {
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
