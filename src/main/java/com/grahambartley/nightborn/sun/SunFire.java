package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public final class SunFire {

  static final AttachmentType<Long> LIT_UNTIL =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "sun_fire_until"));

  private SunFire() {}

  static void lit(final Entity burning, final float seconds) {
    burning.setAttached(
        LIT_UNTIL, burning.getWorld().getTime() + (long) Math.ceil(seconds * 20.0F));
  }

  static void out(final Entity entity) {
    if (entity.hasAttached(LIT_UNTIL)) {
      entity.removeAttached(LIT_UNTIL);
    }
  }

  public static boolean burning(final Entity entity) {
    return litBySun(
        entity.getWorld().getTime(), entity.getAttachedOrElse(LIT_UNTIL, Long.MIN_VALUE));
  }

  public static boolean litBySun(final long now, final long litUntil) {
    return now < litUntil;
  }
}
