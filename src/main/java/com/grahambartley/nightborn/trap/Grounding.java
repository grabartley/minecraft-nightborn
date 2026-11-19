package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.levitation.Levitation;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public final class Grounding {

  static final double DRAG = -0.8;

  public static final AttachmentType<Long> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "grounded"),
          builder -> builder.syncWith(PacketCodecs.VAR_LONG.cast(), AttachmentSyncPredicate.all()));

  private Grounding() {}

  static void register() {}

  public static boolean on(final Entity entity) {
    final Long until = entity.getAttached(TYPE);
    return until != null && entity.getWorld().getTime() < until;
  }

  public static void start(final LivingEntity target, final int ticks) {
    target.setAttached(TYPE, target.getWorld().getTime() + ticks);
    pull(target);
  }

  public static void tick(final LivingEntity entity) {
    final Long until = entity.getAttached(TYPE);
    if (until == null) {
      return;
    }
    if (entity.getWorld().getTime() >= until || !entity.isAlive()) {
      entity.removeAttached(TYPE);
      return;
    }
    pull(entity);
  }

  public static double fall(final double fallingSpeed) {
    return Math.min(fallingSpeed, DRAG);
  }

  public static void unwing(final LivingEntity entity) {
    if (BatForm.isBat(entity)) {
      BatForm.leave(entity);
    }
    if (Levitation.isLevitating(entity)) {
      Levitation.land(entity);
    }
  }

  private static void pull(final LivingEntity entity) {
    unwing(entity);
    if (!entity.isOnGround()) {
      entity.setVelocity(
          entity.getVelocity().x * 0.5, fall(entity.getVelocity().y), entity.getVelocity().z * 0.5);
      entity.velocityModified = true;
    }
  }
}
