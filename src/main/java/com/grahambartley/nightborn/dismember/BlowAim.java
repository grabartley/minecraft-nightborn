package com.grahambartley.nightborn.dismember;

import com.grahambartley.nightborn.fight.BlowReading;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class BlowAim {

  static final double REACH = 8.0;

  private BlowAim() {}

  public static Optional<BodyPart> aimed(
      final LivingEntity victim,
      final DamageSource source,
      final BodyZones zones,
      final Random random) {
    final Entity direct = source.getSource();
    final Entity attacker = source.getAttacker();
    return switch (SeverAim.of(
        BlowReading.blast(source),
        source.isDirect(),
        direct instanceof ProjectileEntity,
        attacker instanceof PlayerEntity)) {
      case LINE_OF_SIGHT -> {
        final Vec3d eye = attacker.getEyePos();
        yield HitLocation.part(
            zones,
            victim.getBoundingBox(),
            eye,
            eye.add(attacker.getRotationVec(1.0F).multiply(REACH)),
            victim.getBodyYaw());
      }
      case IMPACT -> {
        final Vec3d at = direct.getPos();
        final Vec3d step = direct.getVelocity();
        yield HitLocation.part(
            zones,
            victim.getBoundingBox(),
            at.subtract(step),
            at.add(step.multiply(2.0)),
            victim.getBodyYaw());
      }
      case MOB_WEIGHTS -> Optional.of(zones.weighted(random.nextDouble()));
      case RANDOM_LIMB -> {
        final List<BodyPart> limbs = BodyPart.limbs();
        yield Optional.of(limbs.get(random.nextInt(limbs.size())));
      }
    };
  }
}
