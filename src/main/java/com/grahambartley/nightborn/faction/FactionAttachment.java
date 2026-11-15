package com.grahambartley.nightborn.faction;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class FactionAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "faction");

  public static final AttachmentSyncPredicate SYNC_PREDICATE = AttachmentSyncPredicate.targetOnly();

  public static final AttachmentType<Faction> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(Faction.CODEC)
                  .copyOnDeath()
                  .syncWith(Faction.PACKET_CODEC.cast(), SYNC_PREDICATE));

  private FactionAttachment() {}

  public static void register() {}

  public static Faction get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, Faction.DEFAULT);
  }

  public static void set(final AttachmentTarget target, final Faction faction) {
    target.setAttached(TYPE, faction == Faction.DEFAULT ? null : faction);
  }
}
