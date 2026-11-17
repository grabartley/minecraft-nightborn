package com.grahambartley.nightborn.vanish;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public final class VanishAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "vanished_until");

  public static final AttachmentType<Long> TYPE =
      AttachmentRegistry.create(
          ID,
          builder -> builder.syncWith(PacketCodecs.VAR_LONG.cast(), AttachmentSyncPredicate.all()));

  private VanishAttachment() {}

  public static void register() {}

  public static boolean isGone(final Entity entity) {
    return entity.hasAttached(TYPE);
  }

  public static boolean due(final long now, final Long until) {
    return until != null && now >= until;
  }
}
