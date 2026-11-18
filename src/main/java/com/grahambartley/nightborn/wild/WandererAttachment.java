package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class WandererAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "wanderer");

  public static final AttachmentType<Wanderer> TYPE =
      AttachmentRegistry.create(ID, builder -> builder.persistent(Wanderer.CODEC));

  private WandererAttachment() {}

  static void register() {}

  public static Optional<Wanderer> get(final AttachmentTarget target) {
    return Optional.ofNullable(target.getAttached(TYPE));
  }

  public static void set(final AttachmentTarget target, final Wanderer wanderer) {
    target.setAttached(TYPE, wanderer);
  }

  public static void clear(final AttachmentTarget target) {
    target.removeAttached(TYPE);
  }
}
