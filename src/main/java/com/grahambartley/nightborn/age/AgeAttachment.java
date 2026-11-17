package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class AgeAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "age");

  public static final AttachmentType<AgeProgress> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(AgeProgress.CODEC)
                  .copyOnDeath()
                  .syncWith(AgeProgress.PACKET_CODEC.cast(), AttachmentSyncPredicate.targetOnly()));

  public static final AttachmentType<NightWatch> WATCH =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "age_night"),
          builder -> builder.persistent(NightWatch.CODEC).copyOnDeath());

  public static final AttachmentType<Long> CLOCK =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "age_clock"));

  private AgeAttachment() {}

  public static void register() {}

  public static AgeProgress get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, AgeProgress.EMPTY);
  }

  public static void set(final AttachmentTarget target, final AgeProgress progress) {
    if (progress.equals(get(target))) {
      return;
    }
    target.setAttached(TYPE, progress.equals(AgeProgress.EMPTY) ? null : progress);
  }

  public static void clear(final AttachmentTarget target) {
    target.removeAttached(TYPE);
    target.removeAttached(WATCH);
    target.removeAttached(CLOCK);
  }
}
