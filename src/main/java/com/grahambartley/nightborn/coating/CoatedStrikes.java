package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.gear.VampireBane;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.ward.Wards;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

public final class CoatedStrikes {

  private CoatedStrikes() {}

  public static float weaponBonus(final ItemStack weapon, final Entity target) {
    return weapon.isIn(Coatings.WEAPONS) ? bonus(weapon, target) : 0.0F;
  }

  public static float ammoBonus(final ItemStack ammo, final Entity target) {
    return Coatings.isAmmo(ammo) ? bonus(ammo, target) : 0.0F;
  }

  static void afterDamage(
      final LivingEntity target,
      final DamageSource source,
      final float baseDamage,
      final float damage,
      final boolean blocked) {
    final ItemStack ammo = VampireBane.ammo(source);
    final ItemStack weapon =
        source.getWeaponStack() == null ? ItemStack.EMPTY : source.getWeaponStack();
    switch (CoatedHit.of(
        Vampires.isVampire(target),
        blocked,
        Coatings.isCoatedAmmo(ammo),
        Coatings.isCoatedWeapon(weapon),
        source.isIn(Coatings.WEAPON_STRIKES))) {
      case AMMO -> {
        final Coat coat = ammo.get(Coatings.COAT);
        weaken(target, coat.coating(), terms(coat, ammo));
      }
      case WEAPON -> spend(target, weapon);
      case NONE -> {}
    }
  }

  private static void spend(final LivingEntity target, final ItemStack weapon) {
    final long now = target.getWorld().getTime();
    final Coat coat = weapon.get(Coatings.COAT);
    final CoatingTerms terms = terms(coat, weapon);
    if (coat.live(terms, now, false)) {
      weaken(target, coat.coating(), terms);
    }
    coat.afterStrike(terms, now)
        .ifPresentOrElse(
            left -> weapon.set(Coatings.COAT, left),
            () -> {
              weapon.remove(Coatings.COAT);
              if (target.getWorld() instanceof ServerWorld world) {
                coat.coating().wearOff(world, target);
              }
            });
  }

  private static void weaken(
      final LivingEntity vampire, final Coating coating, final CoatingTerms terms) {
    final int strength =
        Wards.TOUCH.equals(coating.weakenedSource())
            ? Wards.dulled(vampire, terms.weakenStrength())
            : terms.weakenStrength();
    Weakened.add(vampire, coating.weakenedSource(), strength, terms.weakenTicks());
  }

  private static float bonus(final ItemStack stack, final Entity target) {
    if (!(target instanceof LivingEntity living) || !Vampires.isVampire(living)) {
      return 0.0F;
    }
    return Coatings.live(stack, ServerConfigService.current(), target.getWorld().getTime())
        .map(coat -> (float) terms(coat, stack).damage())
        .orElse(0.0F);
  }

  private static CoatingTerms terms(final Coat coat, final ItemStack stack) {
    return CoatingTerms.of(coat.coating(), ServerConfigService.current(), stack);
  }
}
