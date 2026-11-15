package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import java.util.Objects;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class StarvingAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "starving");

  public static final AttachmentType<Integer> TYPE =
      AttachmentRegistry.create(
          ID, builder -> builder.persistent(Codec.intRange(0, Integer.MAX_VALUE)));

  private StarvingAttachment() {}

  public static void register() {}

  public static int get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, 0);
  }

  public static void set(final AttachmentTarget target, final int ticks) {
    final Integer stored = ticks <= 0 ? null : ticks;
    if (!Objects.equals(stored, target.getAttached(TYPE))) {
      target.setAttached(TYPE, stored);
    }
  }
}
