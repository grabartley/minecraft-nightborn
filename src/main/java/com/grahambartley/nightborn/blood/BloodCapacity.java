package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public final class BloodCapacity {

  public static final int MOST_EXTRA = 40;

  public static final AttachmentType<Integer> EXTRA =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "blood_capacity"),
          builder ->
              builder
                  .persistent(Codec.intRange(0, MOST_EXTRA))
                  .copyOnDeath()
                  .syncWith(PacketCodecs.VAR_INT.cast(), AttachmentSyncPredicate.targetOnly()));

  private BloodCapacity() {}

  public static int clampExtra(final int extra) {
    return Math.max(0, Math.min(MOST_EXTRA, extra));
  }

  public static final int MOST =
      BloodAttachment.FULL + MOST_EXTRA + (int) VampireAttributes.MOST_EXTRA_BLOOD;

  public static int of(final AttachmentTarget target) {
    return capacity(
        target.getAttachedOrElse(EXTRA, 0),
        target instanceof LivingEntity living
            ? VampireAttributes.value(living, VampireAttributes.EXTRA_BLOOD)
            : 0.0);
  }

  public static int capacity(final int ageExtra, final double skillExtra) {
    return BloodAttachment.FULL
        + clampExtra(ageExtra)
        + (int) Math.max(0.0, Math.min(VampireAttributes.MOST_EXTRA_BLOOD, skillExtra));
  }

  public static void setExtra(final AttachmentTarget target, final int extra) {
    final int clamped = clampExtra(extra);
    if (clamped != target.getAttachedOrElse(EXTRA, 0)) {
      target.setAttached(EXTRA, clamped == 0 ? null : clamped);
    }
    BloodAttachment.set(target, BloodAttachment.get(target));
  }
}
