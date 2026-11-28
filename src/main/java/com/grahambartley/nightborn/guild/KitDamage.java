package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;

public final class KitDamage {

  private KitDamage() {}

  static float of(@Nullable final HunterKit kit, final boolean bolt) {
    return kit != null && kit.ranged() == bolt ? kit.damage() : 0.0F;
  }

  public static float strike(final DamageSource source, final Entity target) {
    return source.isDirect() && source.getAttacker() instanceof GuildFighter fighter
        ? against(vampire(target), fighter.kitDamage(false))
        : 0.0F;
  }

  public static float bolt(@Nullable final Entity owner, final Entity target) {
    return owner instanceof GuildFighter fighter
        ? against(vampire(target), fighter.kitDamage(true))
        : 0.0F;
  }

  static float against(final boolean vampire, final float damage) {
    return vampire ? damage : 0.0F;
  }

  private static boolean vampire(final Entity target) {
    return target instanceof LivingEntity living && Vampires.isVampire(living);
  }
}
