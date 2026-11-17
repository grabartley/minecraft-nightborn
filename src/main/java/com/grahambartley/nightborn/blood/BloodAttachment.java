package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import java.util.Objects;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
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
                  .persistent(Codec.intRange(0, FULL + BloodCapacity.MOST_EXTRA))
                  .syncWith(PacketCodecs.VAR_INT.cast(), AttachmentSyncPredicate.targetOnly()));

  private BloodAttachment() {}

  public static void register() {}

  public static int get(final AttachmentTarget target) {
    final int capacity = BloodCapacity.of(target);
    return Math.min(capacity, target.getAttachedOrElse(TYPE, capacity));
  }

  public static void set(final AttachmentTarget target, final int blood) {
    final Integer stored = stored(blood, BloodCapacity.of(target));
    if (!Objects.equals(stored, target.getAttached(TYPE))) {
      target.setAttached(TYPE, stored);
    }
  }

  static Integer stored(final int blood, final int capacity) {
    return blood >= capacity ? null : Math.max(0, blood);
  }
}
