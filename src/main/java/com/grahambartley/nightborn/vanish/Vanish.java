package com.grahambartley.nightborn.vanish;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.occult.ShadowStep;
import com.grahambartley.nightborn.skill.PowerHeading;
import com.grahambartley.nightborn.smoke.SmokePoof;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class Vanish {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "vanished_until");

  public static final AttachmentType<Long> TYPE =
      AttachmentRegistry.create(
          ID,
          builder -> builder.syncWith(PacketCodecs.VAR_LONG.cast(), AttachmentSyncPredicate.all()));

  private Vanish() {}

  public static void register() {}

  public static boolean isGone(final Entity entity) {
    return entity.hasAttached(TYPE);
  }

  public static Optional<Vec3d> destination(
      final LivingEntity caster, final VanishPower power, final PowerHeading held) {
    final LivingEntity foe = caster instanceof MobEntity mob ? mob.getTarget() : null;
    final Vec3d looking = VanishSpots.facing(caster.getYaw());
    final Vec3d facing =
        foe == null ? looking : VanishSpots.toward(caster.getPos(), foe.getPos(), looking);
    for (final PowerHeading heading :
        VanishSpots.headings(foe != null, held, caster.getRandom().nextBoolean())) {
      final Optional<Vec3d> spot =
          ShadowStep.reach(
              caster, VanishSpots.wanted(caster.getEyePos(), facing, heading, power.distance()));
      if (spot.isPresent()) {
        return spot;
      }
    }
    return Optional.empty();
  }

  public static void go(final LivingEntity caster, final Vec3d to, final VanishPower power) {
    if (caster.getWorld().isClient()) {
      return;
    }
    FeedingSessions.end(caster);
    SmokePoof.burst(caster, BatForm.ENTER_SOUND);
    caster.setAttached(TYPE, caster.getWorld().getTime() + power.goneTicks());
    ShadowStep.move(caster, to);
  }

  public static void tick(final LivingEntity vampire) {
    final Long until = vampire.getAttached(TYPE);
    if (until != null && vampire.getWorld().getTime() >= until) {
      vampire.removeAttached(TYPE);
      SmokePoof.burst(vampire, BatForm.ENTER_SOUND);
    }
  }
}
