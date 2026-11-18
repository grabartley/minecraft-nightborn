package com.grahambartley.nightborn.journal;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class JournalLogAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "journals_given");

  public static final AttachmentType<JournalLog> TYPE =
      AttachmentRegistry.create(ID, builder -> builder.persistent(JournalLog.CODEC).copyOnDeath());

  private JournalLogAttachment() {}

  static void register() {}

  public static JournalLog get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, JournalLog.NONE);
  }

  static void set(final AttachmentTarget target, final JournalLog log) {
    target.setAttached(TYPE, log.given().isEmpty() ? null : log);
  }
}
