package com.grahambartley.nightborn.guild.veteran;

import com.grahambartley.nightborn.ammo.CrossbowAmmo;
import com.grahambartley.nightborn.coating.Coat;
import com.grahambartley.nightborn.coating.Coating;
import com.grahambartley.nightborn.coating.Coatings;
import com.grahambartley.nightborn.guild.Aim;
import com.grahambartley.nightborn.guild.GuildData;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.mind.MindEffect;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.vanish.VanishAttachment;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

final class VeteranStrikes {

  static final float BOLT_SPEED = 1.8F;
  static final float BOLT_SPREAD = 2.0F;
  static final float THROW_SPEED = 0.75F;
  static final float THROW_SPREAD = 1.0F;
  static final int POOL_WAIT_TICKS = 10;
  static final double AIM_HEIGHT = 0.5;

  private VeteranStrikes() {}

  static boolean chain(
      final VeteranHunterEntity veteran,
      final LivingEntity target,
      final VeteranAttack attack,
      final boolean dodged) {
    final boolean lands =
        !dodged
            && target.isAlive()
            && !VanishAttachment.isGone(target)
            && veteran.canSee(target)
            && veteran.distanceTo(target) <= attack.reach();
    if (!lands) {
      return false;
    }
    Landing.move(
        target, VeteranAim.dragSpot(veteran.getPos(), target.getPos(), VeteranAim.DRAG_GAP));
    Grounding.ground(target, attack.effectTicks());
    veteran.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(), 1.0F, 0.8F);
    return true;
  }

  static void volley(
      final VeteranHunterEntity veteran, final Vec3d aim, final VeteranAttack attack) {
    final ItemStack bow = stack(attack.held());
    for (int i = 0; i < attack.count(); i++) {
      final ItemStack bolt = stack(attack.ammo());
      if (!(bolt.getItem() instanceof CrossbowAmmo ammo)) {
        return;
      }
      if (bolt.isIn(Coatings.AMMO)) {
        bolt.set(Coatings.COAT, Coat.fresh(Coating.HOLY_WATER));
      }
      launch(
          veteran,
          ammo.createProjectile(veteran.getWorld(), veteran, bolt, bow, false),
          aim.add(0.0, AIM_HEIGHT, 0.0),
          BOLT_SPEED,
          BOLT_SPREAD);
    }
    veteran.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0F, 0.9F);
  }

  static void arc(
      final VeteranHunterEntity veteran, final List<Vec3d> spots, final VeteranAttack attack) {
    if (!(veteran.getWorld() instanceof ServerWorld world)) {
      return;
    }
    for (final Vec3d spot : spots) {
      final PotionEntity potion = new PotionEntity(world, veteran);
      potion.setItem(PotionContentsComponent.createStack(Items.SPLASH_POTION, HolyWater.POTION));
      launch(veteran, potion, spot, THROW_SPEED, THROW_SPREAD);
      final AreaEffectCloudEntity pool = new AreaEffectCloudEntity(world, spot.x, spot.y, spot.z);
      pool.setOwner(veteran);
      pool.setRadius((float) attack.radius());
      pool.setRadiusOnUse(0.0F);
      pool.setRadiusGrowth(0.0F);
      pool.setWaitTime(POOL_WAIT_TICKS);
      pool.setDuration(attack.effectTicks());
      pool.setPotionContents(new PotionContentsComponent(HolyWater.POTION));
      world.spawnEntity(pool);
    }
    veteran.playSound(SoundEvents.ENTITY_SPLASH_POTION_THROW, 1.0F, 0.8F);
  }

  static void rally(final VeteranHunterEntity veteran, final VeteranAttack attack) {
    for (final HunterEntity hunter : allies(veteran, attack.radius())) {
      hunter.heal(attack.heal());
      hunter.addStatusEffect(
          new StatusEffectInstance(StatusEffects.SPEED, attack.effectTicks()), veteran);
      hunter.removeStatusEffect(MindEffect.MESMERISED);
      hunter.removeAttached(MindTies.MESMERISED_BY);
    }
  }

  static List<HunterEntity> allies(final VeteranHunterEntity veteran, final double radius) {
    return veteran
        .getWorld()
        .getEntitiesByClass(
            HunterEntity.class,
            new Box(veteran.getBlockPos()).expand(radius),
            hunter -> hunter.isAlive() && hunter.squaredDistanceTo(veteran) <= radius * radius);
  }

  private static void launch(
      final VeteranHunterEntity veteran,
      final ProjectileEntity projectile,
      final Vec3d at,
      final float speed,
      final float spread) {
    final double dx = at.x - veteran.getX();
    final double dz = at.z - veteran.getZ();
    projectile.setVelocity(dx, Aim.rise(at.y, projectile.getY(), dx, dz), dz, speed, spread);
    veteran.getWorld().spawnEntity(projectile);
  }

  private static ItemStack stack(final Optional<Identifier> id) {
    return id.flatMap(GuildData::item).map(ItemStack::new).orElse(ItemStack.EMPTY);
  }
}
