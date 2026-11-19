package com.grahambartley.nightborn.guild.veteran;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class Grounding {

  static final Identifier HELD_DOWN = Identifier.of(Nightborn.MOD_ID, "grounded");

  public static final AttachmentType<Long> UNTIL =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "grounded_until"));

  private Grounding() {}

  static void register() {}

  public static void ground(final LivingEntity vampire, final int ticks) {
    if (ticks <= 0) {
      return;
    }
    final long until = vampire.getWorld().getTime() + ticks;
    vampire.setAttached(UNTIL, Math.max(until, vampire.getAttachedOrElse(UNTIL, until)));
    BatForm.leave(vampire);
    Levitation.land(vampire);
  }

  public static boolean held(final long now, final Long until) {
    return until != null && now < until;
  }

  public static double rise(final double velocityY, final boolean held) {
    return held ? Math.min(0.0, velocityY) : velocityY;
  }

  public static void tick(final LivingEntity vampire) {
    final Long until = vampire.getAttached(UNTIL);
    if (until == null) {
      return;
    }
    final boolean held = held(vampire.getWorld().getTime(), until);
    if (!held) {
      vampire.removeAttached(UNTIL);
    } else {
      BatForm.leave(vampire);
      Levitation.land(vampire);
      final Vec3d velocity = vampire.getVelocity();
      if (velocity.y > 0.0) {
        vampire.setVelocity(velocity.x, rise(velocity.y, true), velocity.z);
        vampire.velocityModified = true;
      }
    }
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_JUMP_STRENGTH,
        HELD_DOWN,
        held ? -1.0 : 0.0,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
  }
}
