package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class HuntAttachment {

  public static final AttachmentType<HuntRecord> RECORD =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "hunt"),
          builder -> builder.persistent(HuntRecord.CODEC).copyOnDeath());

  public static final AttachmentType<Long> CLOCK =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "hunt_clock"), builder -> builder.copyOnDeath());

  public static final AttachmentType<BellCall> CALL =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "bell_call"));

  private HuntAttachment() {}

  public static void register() {}

  public static HuntRecord get(final AttachmentTarget target) {
    return target.getAttachedOrElse(RECORD, HuntRecord.EMPTY);
  }

  public static void clear(final AttachmentTarget target) {
    target.removeAttached(RECORD);
    target.removeAttached(CLOCK);
    target.removeAttached(CALL);
  }
}
