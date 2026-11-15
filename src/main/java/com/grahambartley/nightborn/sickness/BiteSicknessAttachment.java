package com.grahambartley.nightborn.sickness;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class BiteSicknessAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "bite_sickness");

  public static final AttachmentType<Integer> TICKS_LEFT =
      AttachmentRegistry.create(
          ID, builder -> builder.persistent(Codec.intRange(0, Integer.MAX_VALUE)).copyOnDeath());

  private BiteSicknessAttachment() {}

  public static void register() {}

  public static boolean isSick(final AttachmentTarget target) {
    return target.hasAttached(TICKS_LEFT);
  }

  public static int ticksLeft(final AttachmentTarget target) {
    return target.getAttachedOrElse(TICKS_LEFT, 0);
  }

  public static void set(final AttachmentTarget target, final int ticksLeft) {
    target.setAttached(TICKS_LEFT, ticksLeft);
  }

  public static void clear(final AttachmentTarget target) {
    target.removeAttached(TICKS_LEFT);
  }
}
