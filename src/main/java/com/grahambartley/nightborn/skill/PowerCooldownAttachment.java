package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class PowerCooldownAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "power_cooldowns");

  public static final AttachmentType<PowerCooldowns> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(PowerCooldowns.CODEC)
                  .copyOnDeath()
                  .syncWith(
                      PowerCooldowns.PACKET_CODEC.cast(), AttachmentSyncPredicate.targetOnly()));

  private PowerCooldownAttachment() {}

  public static void register() {}

  public static PowerCooldowns get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, PowerCooldowns.NONE);
  }

  public static void set(final AttachmentTarget target, final PowerCooldowns cooldowns) {
    target.setAttached(TYPE, cooldowns.readyAt().isEmpty() ? null : cooldowns);
  }
}
