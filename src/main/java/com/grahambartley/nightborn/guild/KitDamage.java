package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

public final class KitDamage {

  private KitDamage() {}

  static float blow(@Nullable final HunterKit kit, final ItemStack weapon) {
    return kit != null
            && !kit.ranged()
            && !weapon.isEmpty()
            && Registries.ITEM.getId(weapon.getItem()).equals(kit.weapon())
        ? kit.damage()
        : 0.0F;
  }

  static float bolt(@Nullable final HunterKit kit) {
    return kit != null && kit.ranged() ? kit.damage() : 0.0F;
  }

  public static float strike(final DamageSource source, final Entity target) {
    final ItemStack weapon = source.getWeaponStack();
    return source.isDirect()
            && weapon != null
            && source.getAttacker() instanceof GuildFighter fighter
        ? against(vampire(target), fighter.blowDamage(weapon))
        : 0.0F;
  }

  public static float shot(@Nullable final Entity owner, final Entity target) {
    return owner instanceof GuildFighter fighter
        ? against(vampire(target), fighter.boltDamage())
        : 0.0F;
  }

  static float against(final boolean vampire, final float damage) {
    return vampire ? damage : 0.0F;
  }

  private static boolean vampire(final Entity target) {
    return target instanceof LivingEntity living && Vampires.isVampire(living);
  }
}
