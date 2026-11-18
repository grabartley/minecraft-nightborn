package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class NightLogAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "night_log");

  public static final AttachmentType<NightLog> TYPE =
      AttachmentRegistry.create(ID, builder -> builder.persistent(NightLog.CODEC).copyOnDeath());

  private NightLogAttachment() {}

  static void register() {}

  public static Optional<NightLog> get(final AttachmentTarget target) {
    return Optional.ofNullable(target.getAttached(TYPE));
  }

  public static void set(final AttachmentTarget target, final NightLog log) {
    target.setAttached(TYPE, log);
  }
}
