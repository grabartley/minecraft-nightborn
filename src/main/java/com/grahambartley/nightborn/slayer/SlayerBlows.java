package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.skill.Boosts;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;

public final class SlayerBlows {

  private SlayerBlows() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> {
          if (taken > 0.0F && entity.isAlive() && struck(source)) {
            downs((LivingEntity) source.getAttacker(), entity);
          }
        });
  }

  public static float raise(final float amount, final double flat, final double share) {
    return (float) ((amount + Math.max(0.0, flat)) * (1.0 + Math.max(0.0, share)));
  }

  public static float scale(
      final LivingEntity target, final DamageSource source, final float amount) {
    if (!struck(source) || !Vampires.isVampire(target)) {
      return amount;
    }
    final LivingEntity hunter = (LivingEntity) source.getAttacker();
    return raise(
        amount,
        HunterAttributes.value(hunter, HunterAttributes.VAMPIRE_DAMAGE),
        HunterAttributes.value(hunter, HunterAttributes.VAMPIRE_DAMAGE_SHARE));
  }

  static boolean struck(final DamageSource source) {
    return source.getAttacker() instanceof LivingEntity
        && (source.getSource() == source.getAttacker()
            || source.isIn(DamageTypeTags.IS_PROJECTILE));
  }

  private static void downs(final LivingEntity hunter, final LivingEntity target) {
    for (final DownBlow blow : Boosts.downs(hunter)) {
      if (SlayerDowns.down(hunter, target, blow)) {
        return;
      }
    }
  }
}
