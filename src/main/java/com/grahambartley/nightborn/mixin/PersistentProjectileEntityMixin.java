package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.gear.VampireBane;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
abstract class PersistentProjectileEntityMixin {

  @Shadow private double damage;

  @Unique private double nightborn$baseDamage;

  @Inject(method = "onEntityHit", at = @At("HEAD"))
  private void nightborn$addBane(final EntityHitResult hit, final CallbackInfo ci) {
    final PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
    nightborn$baseDamage = damage;
    damage =
        VampireBane.projectileBase(
            nightborn$baseDamage,
            VampireBane.ammoBonus(self.getItemStack(), hit.getEntity()),
            self.getVelocity().length());
  }

  @Inject(method = "onEntityHit", at = @At("RETURN"))
  private void nightborn$restoreDamage(final EntityHitResult hit, final CallbackInfo ci) {
    damage = nightborn$baseDamage;
  }
}
