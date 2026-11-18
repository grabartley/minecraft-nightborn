package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class SunShafts {

  public static final AttachmentType<Long> LIT =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "sun_shaft"));

  private SunShafts() {}

  public static void register() {}

  public static void light(final LivingEntity entity, final int ticks) {
    final long until = entity.getWorld().getTime() + ticks;
    entity.setAttached(LIT, Math.max(until, entity.getAttachedOrElse(LIT, Long.MIN_VALUE)));
  }

  public static boolean lit(final LivingEntity entity) {
    return litAt(entity.getAttached(LIT), entity.getWorld().getTime());
  }

  public static boolean litAt(final Long until, final long now) {
    return until != null && now < until;
  }
}
