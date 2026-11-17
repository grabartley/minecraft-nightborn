package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.occult.ShadowStep;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class BossReactions {

  static final float LOUD = 2.0F;
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

  public static void guard(
      final MobEntity boss, final Entity attacker, final PhaseDefence defence) {
    boss.playSound(SoundEvents.ITEM_SHIELD_BLOCK, LOUD, 0.7F);
    boss.swingHand(boss.getActiveHand());
    if (attacker instanceof LivingEntity foe && defence.guardKnock() > 0.0F) {
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

  public static NbtList list(final Vec3d vec) {
    final NbtList list = new NbtList();
    list.add(NbtDouble.of(vec.x));
    list.add(NbtDouble.of(vec.y));
    list.add(NbtDouble.of(vec.z));
    return list;
  }

  public static Optional<Vec3d> vec(final NbtCompound nbt, final String key) {
    final NbtList list = nbt.getList(key, NbtElement.DOUBLE_TYPE);
    return list.size() == 3
        ? Optional.of(new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2)))
        : Optional.empty();
  }
}
