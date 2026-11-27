package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class RiteCooldownAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "rite_ready_at");
  public static final long READY = 0L;

  public static final AttachmentType<Long> TYPE =
      AttachmentRegistry.create(ID, builder -> builder.persistent(Codec.LONG).copyOnDeath());

  private RiteCooldownAttachment() {}

  public static void register() {}

  public static long readyAt(final AttachmentTarget player) {
    return player.getAttachedOrElse(TYPE, READY);
  }

  public static void wait(final AttachmentTarget player, final long readyAt) {
    player.setAttached(TYPE, readyAt <= READY ? null : readyAt);
  }
}
