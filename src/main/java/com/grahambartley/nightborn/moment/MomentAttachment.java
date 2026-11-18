package com.grahambartley.nightborn.moment;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class MomentAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "moment");

  public static final AttachmentType<MomentState> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder.syncWith(
                  MomentState.PACKET_CODEC.cast(), AttachmentSyncPredicate.targetOnly()));

  private MomentAttachment() {}

  public static void register() {}

  @Nullable
  public static MomentState get(final AttachmentTarget target) {
    return target.getAttached(TYPE);
  }

  public static void set(final AttachmentTarget target, @Nullable final MomentState state) {
    target.setAttached(TYPE, state);
  }
}
