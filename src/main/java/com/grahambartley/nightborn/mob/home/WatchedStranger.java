package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class WatchedStranger {

  public static final AttachmentType<UUID> TYPE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "watched_stranger"));

  private WatchedStranger() {}

  public static Optional<UUID> of(final AttachmentTarget watcher) {
    return Optional.ofNullable(watcher.getAttached(TYPE));
  }

  public static void watch(final AttachmentTarget watcher, final UUID stranger) {
    watcher.setAttached(TYPE, stranger);
  }

  public static void stop(final AttachmentTarget watcher) {
    watcher.removeAttached(TYPE);
  }
}
