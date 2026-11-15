package com.grahambartley.nightborn.ammo;

import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class GarlicBombEntity extends ThrownItemEntity {

  static final int BURST_PARTICLES = 24;
  static final double BURST_SPREAD = 0.25;
  static final double BURST_SPEED = 0.12;

  public GarlicBombEntity(final EntityType<? extends GarlicBombEntity> type, final World world) {
    super(type, world);
  }

  GarlicBombEntity(final World world, final LivingEntity owner) {
    super(AmmoEntities.GARLIC_BOMB, owner, world);
  }

  GarlicBombEntity(final World world, final double x, final double y, final double z) {
    super(AmmoEntities.GARLIC_BOMB, x, y, z, world);
  }

  @Override
  protected Item getDefaultItem() {
    return AmmoItems.GARLIC_BOMB;
  }

  @Override
  protected void onCollision(final HitResult hit) {
    super.onCollision(hit);
    if (getWorld() instanceof ServerWorld world) {
      burst(world);
      discard();
    }
  }

  private void burst(final ServerWorld world) {
    final GarlicCloud shape = GarlicCloud.of(ServerConfigService.current().gear());
    final AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, getX(), getY(), getZ());
    if (getOwner() instanceof LivingEntity owner) {
      cloud.setOwner(owner);
    }
    cloud.setRadius(shape.radius());
    cloud.setDuration(shape.durationTicks());
    cloud.setRadiusGrowth(shape.radiusGrowth());
    cloud.setRadiusOnUse(0.0F);
    cloud.setWaitTime(0);
    cloud.setPotionContents(
        new PotionContentsComponent(
            Optional.empty(),
            Optional.of(GarlicFumesEffect.COLOUR),
            List.of(new StatusEffectInstance(GarlicFumesEffect.ENTRY, 1))));
    world.spawnEntity(cloud);
    world.spawnParticles(
        new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(GearItems.GARLIC)),
        getX(),
        getY(),
        getZ(),
        BURST_PARTICLES,
        BURST_SPREAD,
        BURST_SPREAD,
        BURST_SPREAD,
        BURST_SPEED);
    world.playSound(
        null,
        getBlockPos(),
        SoundEvents.ENTITY_SPLASH_POTION_BREAK,
        SoundCategory.NEUTRAL,
        1.0F,
        0.7F + world.random.nextFloat() * 0.1F);
  }
}
