package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.Nightborn;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class QuarryBrand {

  public static final AttachmentType<Long> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "quarried"),
          builder -> builder.syncWith(PacketCodecs.VAR_LONG, AttachmentSyncPredicate.all()));

  private QuarryBrand() {}

  static void register() {}

  static Optional<Long> latest(final Collection<Long> untils) {
    return untils.stream().max(Long::compare);
  }

  static void refresh(
      final MinecraftServer server, final UUID target, final Collection<Long> untils) {
    for (final ServerWorld world : server.getWorlds()) {
      final Entity entity = world.getEntity(target);
      if (entity != null) {
        latest(untils)
            .ifPresentOrElse(
                until -> entity.setAttached(TYPE, until), () -> entity.removeAttached(TYPE));
        return;
      }
    }
  }
}
