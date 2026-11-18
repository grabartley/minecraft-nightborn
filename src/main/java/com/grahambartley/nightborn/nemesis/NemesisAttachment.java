package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class NemesisAttachment {

  public static final AttachmentType<NemesisMark> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "nemesis"),
          builder -> builder.persistent(NemesisMark.CODEC));

  private NemesisAttachment() {}

  static void register() {}

  public static Optional<NemesisMark> get(final AttachmentTarget target) {
    return Optional.ofNullable(target.getAttached(TYPE));
  }

  static void set(final AttachmentTarget target, final NemesisMark mark) {
    target.setAttached(TYPE, mark);
  }

  static void clear(final AttachmentTarget target) {
    target.removeAttached(TYPE);
  }
}
