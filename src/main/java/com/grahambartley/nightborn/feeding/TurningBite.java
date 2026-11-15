package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public final class TurningBite {

  public static final AttachmentType<Boolean> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "turning_bite"),
          builder ->
              builder
                  .persistent(Codec.BOOL)
                  .copyOnDeath()
                  .syncWith(PacketCodecs.BOOL.cast(), AttachmentSyncPredicate.targetOnly()));

  private TurningBite() {}

  public static boolean isOn(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, false);
  }

  public static void set(final AttachmentTarget target, final boolean on) {
    if (on) {
      target.setAttached(TYPE, true);
    } else {
      target.removeAttached(TYPE);
    }
  }
}
