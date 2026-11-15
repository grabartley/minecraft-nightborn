package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.skill.PowerShow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.TridentItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public final class CombatMoves {

  private long nextMove;

  public <T extends MobEntity & PowerShow> void tick(final T fighter, final StageCombat combat) {
    final LivingEntity target = fighter.getTarget();
    final long now = fighter.getWorld().getTime();
    if (target == null || now < nextMove || !fighter.isOnGround()) {
      return;
    }
    nextMove = now + combat.thinkTicks();
    final CombatMove.Threat threat =
        new CombatMove.Threat(
            target.handSwinging, target.isUsingItem() && ranged(target.getActiveItem()));
    CombatMove.decide(
            fighter.distanceTo(target), true, threat, fighter.getRandom().nextInt(100), combat)
        .ifPresent(
            move -> {
              final Vec3d toward = target.getPos().subtract(fighter.getPos());
              fighter.setVelocity(
                  fighter
                      .getVelocity()
                      .add(impulse(move, toward, fighter.getRandom().nextBoolean())));
              fighter.velocityModified = true;
              fighter.showPower(move.clip());
              fighter.playSound(sound(move), 1.0F, 1.0F);
              nextMove = now + (long) combat.thinkTicks() * CombatMove.COOLDOWN_THINKS;
            });
  }

  public static Vec3d impulse(final CombatMove move, final Vec3d toward, final boolean left) {
    final Vec3d flat = new Vec3d(toward.x, 0.0, toward.z);
    if (flat.lengthSquared() < 1.0E-6) {
      return new Vec3d(0.0, move.lift(), 0.0);
    }
    final Vec3d ahead = flat.normalize();
    final Vec3d heading =
        move == CombatMove.DODGE
            ? (left ? new Vec3d(-ahead.z, 0.0, ahead.x) : new Vec3d(ahead.z, 0.0, -ahead.x))
            : ahead;
    return heading.multiply(move.push()).add(0.0, move.lift(), 0.0);
  }

  static boolean ranged(final ItemStack stack) {
    return stack.getItem() instanceof RangedWeaponItem || stack.getItem() instanceof TridentItem;
  }

  private static SoundEvent sound(final CombatMove move) {
    return switch (move) {
      case LEAP -> SoundEvents.ENTITY_GOAT_LONG_JUMP;
      case DASH -> SoundEvents.ENTITY_BREEZE_WIND_BURST.value();
      case DODGE -> SoundEvents.ENTITY_BREEZE_SLIDE;
    };
  }
}
