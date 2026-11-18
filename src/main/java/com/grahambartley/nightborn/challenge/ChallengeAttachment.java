package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class ChallengeAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "challenges");

  public static final AttachmentType<ChallengeRecord> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(ChallengeRecord.CODEC)
                  .copyOnDeath()
                  .syncWith(
                      ChallengeRecord.PACKET_CODEC.cast(), AttachmentSyncPredicate.targetOnly()));

  private ChallengeAttachment() {}

  static void register() {}

  public static ChallengeRecord get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, ChallengeRecord.EMPTY);
  }

  public static void set(final AttachmentTarget target, final ChallengeRecord record) {
    target.setAttached(TYPE, record.isEmpty() ? null : record);
  }
}
