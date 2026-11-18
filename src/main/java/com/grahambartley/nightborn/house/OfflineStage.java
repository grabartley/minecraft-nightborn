package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeProgress;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public final class OfflineStage {

  private OfflineStage() {}

  public static Optional<Integer> ageRank(final NbtCompound player) {
    final NbtCompound attachments = player.getCompound(AttachmentTarget.NBT_ATTACHMENT_KEY);
    final String key = AgeAttachment.ID.toString();
    if (!attachments.contains(key)) {
      return Optional.of(AgeProgress.EMPTY.rank());
    }
    return AgeProgress.CODEC
        .parse(NbtOps.INSTANCE, attachments.get(key))
        .result()
        .map(AgeProgress::rank);
  }
}
