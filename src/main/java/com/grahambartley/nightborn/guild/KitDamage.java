package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.gear.VampireBane;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class KitDamage {

  static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "kit_damage");

  private KitDamage() {}

  static float melee(@Nullable final HunterKit kit) {
    return kit == null || kit.ranged() ? 0.0F : kit.damage();
  }

  static float bolt(@Nullable final HunterKit kit) {
    return kit != null && kit.ranged() ? kit.damage() : 0.0F;
  }

  public static void arm(final LivingEntity wielder, @Nullable final HunterKit kit) {
    final EntityAttributeInstance attack =
        wielder.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    if (attack == null) {
      return;
    }
    attack.removeModifier(ID);
    final float extra = melee(kit);
    if (extra > 0.0F) {
      attack.addTemporaryModifier(
          new EntityAttributeModifier(ID, extra, EntityAttributeModifier.Operation.ADD_VALUE));
    }
  }

  public static void sharpen(
      final ProjectileEntity projectile, final float extra, final double speed) {
    if (extra > 0.0F && projectile instanceof PersistentProjectileEntity bolt) {
      bolt.setDamage(VampireBane.projectileBase(bolt.getDamage(), extra, speed));
    }
  }
}
