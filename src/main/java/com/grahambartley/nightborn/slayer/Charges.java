package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class Charges {

  public static final AttachmentType<Long> TYPE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "charge"));

  private Charges() {}

  static void register() {}

  public static void start(final Entity entity) {
    entity.setAttached(TYPE, entity.getWorld().getTime());
  }

  public static void clear(final Entity entity) {
    entity.removeAttached(TYPE);
  }

  public static boolean charging(final Entity entity, final int windowTicks) {
    return within(entity.getAttached(TYPE), entity.getWorld().getTime(), windowTicks);
  }

  public static boolean within(
      @Nullable final Long startedAt, final long now, final int windowTicks) {
    return startedAt != null && now >= startedAt && now - startedAt <= windowTicks;
  }
}
