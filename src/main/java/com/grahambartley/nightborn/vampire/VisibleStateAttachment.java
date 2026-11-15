package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class VisibleStateAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "visible");

  public static final AttachmentType<VisibleState> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder.syncWith(VisibleState.PACKET_CODEC.cast(), AttachmentSyncPredicate.all()));

  private VisibleStateAttachment() {}

  public static void register() {}

  public static VisibleState get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, VisibleState.NONE);
  }

  public static void set(final AttachmentTarget target, final VisibleState state) {
    if (state.equals(get(target))) {
      return;
    }
    target.setAttached(TYPE, state.equals(VisibleState.NONE) ? null : state);
  }
}
