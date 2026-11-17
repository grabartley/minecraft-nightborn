package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.mojang.serialization.Codec;
import java.util.Objects;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public final class BloodAttachment {

  public static final int FULL = 20;

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "blood");

  public static final AttachmentType<Integer> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(Codec.intRange(0, (int) VampireAttributes.MOST_BLOOD))
                  .syncWith(PacketCodecs.VAR_INT.cast(), AttachmentSyncPredicate.targetOnly()));

  private BloodAttachment() {}

  public static void register() {}

  public static int get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, FULL);
  }

  public static int max(final AttachmentTarget target) {
    return target instanceof LivingEntity living
        ? (int) VampireAttributes.value(living, VampireAttributes.MAX_BLOOD)
        : FULL;
  }

  public static void set(final AttachmentTarget target, final int blood) {
    final int clamped = Math.max(0, Math.min(max(target), blood));
    final Integer stored = clamped == FULL ? null : clamped;
    if (!Objects.equals(stored, target.getAttached(TYPE))) {
      target.setAttached(TYPE, stored);
    }
  }
}
