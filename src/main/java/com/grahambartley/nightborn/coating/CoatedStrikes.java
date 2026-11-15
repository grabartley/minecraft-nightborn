package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.gear.VampireBane;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public final class CoatedStrikes {

  static final int PUFF_PARTICLES = 8;

  private CoatedStrikes() {}

  public static float weaponBonus(final ItemStack weapon, final Entity target) {
    return bonus(weapon, target, false);
  }

  public static float ammoBonus(final ItemStack ammo, final Entity target) {
    return bonus(ammo, target, true);
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
    if (ammo.contains(Coatings.COAT)) {
      weaken(target, ammo.get(Coatings.COAT));
    } else if (weapon != null && weapon.contains(Coatings.COAT)) {
      spend(target, weapon);
    }
  }

  static void wearOff(final ServerWorld world, final Entity at, final Coating coating) {
    world.spawnParticles(
        particle(coating),
        at.getX(),
        at.getBodyY(0.5),
        at.getZ(),
        PUFF_PARTICLES,
        at.getWidth() / 2.0,
        at.getHeight() / 4.0,
        at.getWidth() / 2.0,
        0.02);
    world.playSound(null, at.getBlockPos(), sound(coating), SoundCategory.PLAYERS, 0.5F, 1.6F);
  }

  private static void spend(final LivingEntity target, final ItemStack weapon) {
    final long now = target.getWorld().getTime();
    final Coat coat = weapon.get(Coatings.COAT).startingAt(now);
    final CoatingTerms terms = CoatingTerms.of(coat.coating(), ServerConfigService.current());
    if (coat.worn(terms, now)) {
      return;
    }
    weaken(target, coat);
    final Coat spent = coat.struck();
    if (spent.worn(terms, now)) {
      weapon.remove(Coatings.COAT);
      if (target.getWorld() instanceof ServerWorld world) {
        wearOff(world, target, coat.coating());
      }
    } else {
      weapon.set(Coatings.COAT, spent);
    }
  }

  private static void weaken(final LivingEntity vampire, final Coat coat) {
    final CoatingTerms terms = CoatingTerms.of(coat.coating(), ServerConfigService.current());
    Weakened.add(
        vampire, coat.coating().weakenedSource(), terms.weakenStrength(), terms.weakenTicks());
  }

  private static float bonus(final ItemStack stack, final Entity target, final boolean ammo) {
    final Coat coat = stack.get(Coatings.COAT);
    if (coat == null || !(target instanceof LivingEntity living) || !Vampires.isVampire(living)) {
      return 0.0F;
    }
    final CoatingTerms terms = CoatingTerms.of(coat.coating(), ServerConfigService.current());
    return ammo || !coat.worn(terms, target.getWorld().getTime()) ? terms.damage() : 0.0F;
  }

  private static ParticleEffect particle(final Coating coating) {
    return switch (coating) {
      case HOLY_WATER -> ParticleTypes.WHITE_SMOKE;
      case GARLIC ->
          new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(GearItems.GARLIC));
    };
  }

  private static SoundEvent sound(final Coating coating) {
    return switch (coating) {
      case HOLY_WATER -> SoundEvents.BLOCK_FIRE_EXTINGUISH;
      case GARLIC -> SoundEvents.ENTITY_SPLASH_POTION_BREAK;
    };
  }
}
