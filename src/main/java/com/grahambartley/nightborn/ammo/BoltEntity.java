package com.grahambartley.nightborn.ammo;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class BoltEntity extends PersistentProjectileEntity {

  public BoltEntity(final EntityType<? extends BoltEntity> type, final World world) {
    super(type, world);
  }

  BoltEntity(
      final World world,
      final LivingEntity owner,
      final ItemStack stack,
      @Nullable final ItemStack crossbow) {
    super(AmmoEntities.BOLT, owner, world, stack, crossbow);
  }

  BoltEntity(
      final World world, final double x, final double y, final double z, final ItemStack stack) {
    super(AmmoEntities.BOLT, x, y, z, world, stack, null);
  }

  @Override
  protected ItemStack getDefaultItemStack() {
    return new ItemStack(AmmoItems.STAKE_BOLT);
  }

  @Override
  protected void onHit(final LivingEntity target) {
    super.onHit(target);
    final PotionContentsComponent contents = contents();
    if (contents != null) {
      contents.forEachEffect(
          effect -> {
            if (effect.getEffectType().value().isInstant()) {
              effect
                  .getEffectType()
                  .value()
                  .applyInstantEffect(this, getEffectCause(), target, effect.getAmplifier(), 1.0);
            } else {
              target.addStatusEffect(effect, getEffectCause());
            }
          });
    }
  }

  @Override
  protected void onEntityHit(final EntityHitResult hit) {
    super.onEntityHit(hit);
    shatterIfVial();
  }

  @Override
  protected void onBlockHit(final BlockHitResult hit) {
    super.onBlockHit(hit);
    shatterIfVial();
  }

  private void shatterIfVial() {
    final PotionContentsComponent contents = contents();
    if (contents == null || getWorld().isClient || isRemoved()) {
      return;
    }
    getWorld()
        .syncWorldEvent(WorldEvents.SPLASH_POTION_SPLASHED, getBlockPos(), contents.getColor());
    discard();
  }

  @Nullable
  private PotionContentsComponent contents() {
    return getItemStack().get(DataComponentTypes.POTION_CONTENTS);
  }
}
