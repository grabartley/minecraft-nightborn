package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.gear.VampireBane;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;

public final class CoatedStrikes {

  static final int PUFF_PARTICLES = 8;

  private CoatedStrikes() {}

  public static float weaponBonus(final ItemStack weapon, final Entity target) {
    return bonus(weapon, target, false);
  }

  public static float ammoBonus(final ItemStack ammo, final Entity target) {
    return ammo.isIn(Coatings.AMMO) ? bonus(ammo, target, true) : 0.0F;
  }

  static void afterDamage(
      final LivingEntity target,
      final DamageSource source,
      final float baseDamage,
      final float damage,
      final boolean blocked) {
    if (blocked || !Vampires.isVampire(target)) {
      return;
    }
    final ItemStack ammo = VampireBane.ammo(source);
    final ItemStack weapon = source.getWeaponStack();
    if (Coatings.isCoatedAmmo(ammo)) {
      final Coat coat = ammo.get(Coatings.COAT);
      weaken(target, coat.coating(), terms(coat));
    } else if (weapon != null && weapon.contains(Coatings.COAT)) {
      spend(target, weapon);
    }
  }

  static void wearOff(final ServerWorld world, final Entity at, final Coating coating) {
    world.spawnParticles(
        coating.wearOffParticle(),
        at.getX(),
        at.getBodyY(0.5),
        at.getZ(),
        PUFF_PARTICLES,
        at.getWidth() / 2.0,
        at.getHeight() / 4.0,
        at.getWidth() / 2.0,
        0.02);
    world.playSound(
        null, at.getBlockPos(), coating.wearOffSound(), SoundCategory.PLAYERS, 0.5F, 1.6F);
  }

  private static void spend(final LivingEntity target, final ItemStack weapon) {
    final long now = target.getWorld().getTime();
    final Coat coat = weapon.get(Coatings.COAT);
    final CoatingTerms terms = terms(coat);
    if (coat.live(terms, now, false)) {
      weaken(target, coat.coating(), terms);
    }
    coat.afterStrike(terms, now)
        .ifPresentOrElse(
            left -> weapon.set(Coatings.COAT, left),
            () -> {
              weapon.remove(Coatings.COAT);
              if (target.getWorld() instanceof ServerWorld world) {
                wearOff(world, target, coat.coating());
              }
            });
  }

  private static void weaken(
      final LivingEntity vampire, final Coating coating, final CoatingTerms terms) {
    Weakened.add(vampire, coating.weakenedSource(), terms.weakenStrength(), terms.weakenTicks());
  }

  private static float bonus(final ItemStack stack, final Entity target, final boolean ammo) {
    final Coat coat = stack.get(Coatings.COAT);
    if (coat == null || !(target instanceof LivingEntity living) || !Vampires.isVampire(living)) {
      return 0.0F;
    }
    return coat.bonus(terms(coat), target.getWorld().getTime(), ammo);
  }

  private static CoatingTerms terms(final Coat coat) {
    return CoatingTerms.of(coat.coating(), ServerConfigService.current());
  }
}
