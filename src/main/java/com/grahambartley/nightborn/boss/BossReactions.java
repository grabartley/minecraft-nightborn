package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.occult.ShadowStep;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class BossReactions {

  static final double CLOSE = 5.0;
  static final double EDGE = 2.0;

  private BossReactions() {}

  public static Vec3d away(
      final Vec3d self, final Vec3d foe, final double blocks, @Nullable final Arena arena) {
    final Vec3d flat = new Vec3d(self.x - foe.x, 0.0, self.z - foe.z);
    final Vec3d heading =
        flat.lengthSquared() < 1.0E-6 ? new Vec3d(1.0, 0.0, 0.0) : flat.normalize();
    final Vec3d wanted = self.add(heading.multiply(blocks));
    return arena == null ? wanted : arena.pullIn(wanted, EDGE);
  }

  public static boolean throwsBack(final float knock, final double distance) {
    return knock > 0.0F && distance <= CLOSE;
  }

  public static void guard(
      final MobEntity boss, final Entity attacker, final PhaseDefence defence) {
    boss.playSound(SoundEvents.ITEM_SHIELD_BLOCK, BossAttacks.LOUD, 0.7F);
    boss.swingHand(boss.getActiveHand());
    if (attacker instanceof LivingEntity foe
        && throwsBack(defence.guardKnock(), boss.distanceTo(foe))) {
      foe.takeKnockback(defence.guardKnock(), boss.getX() - foe.getX(), boss.getZ() - foe.getZ());
      foe.velocityModified = true;
    }
  }

  public static void breakAway(
      final MobEntity boss,
      final Entity attacker,
      final PhaseDefence defence,
      @Nullable final Arena arena) {
    returnTo(boss, away(boss.getPos(), attacker.getPos(), defence.breakAway(), arena));
  }

  public static void returnTo(final MobEntity boss, @Nullable final Vec3d spot) {
    if (spot == null || boss.getPos().squaredDistanceTo(spot) < 1.0) {
      return;
    }
    boss.getNavigation().stop();
    Landing.reach(boss, spot.add(0.0, boss.getStandingEyeHeight(), 0.0))
        .ifPresent(to -> ShadowStep.step(boss, to));
  }

  public static void strike(final MobEntity boss) {
    final LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(boss.getWorld());
    if (bolt != null) {
      bolt.refreshPositionAfterTeleport(boss.getPos());
      bolt.setCosmetic(true);
      boss.getWorld().spawnEntity(bolt);
    }
  }
}
