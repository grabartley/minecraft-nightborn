package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;

final class Feeder {

  static final double RESIST_PUSH = 0.8;

  private Feeder() {}

  enum Result {
    CONTINUE,
    DRAINED,
    RESISTED
  }

  static Result drink(
      final ServerWorld world,
      final ServerPlayerEntity vampire,
      final LivingEntity victim,
      final FeedingSession session,
      final NightbornConfig config) {
    final Drinking.Sip sip =
        Drinking.next(
            victim.getHealth(), config.feeding().drinkPerSecond(), session.atLastHealth());
    session.atLastHealth(sip.atLastHealth());
    final DamageSource bite = NightbornDamage.bite(world, vampire);
    if (sip.take() > 0.0F) {
      final float before = victim.getHealth();
      victim.damage(bite, sip.drains() ? sip.take() + victim.getAbsorptionAmount() : sip.take());
      final float taken = Math.max(0.0F, before - Math.max(0.0F, victim.getHealth()));
      session.took(taken);
      keep(vampire, session, taken, config);
      world.playSound(
          null,
          vampire.getBlockPos(),
          SoundEvents.ENTITY_GENERIC_DRINK,
          SoundCategory.PLAYERS,
          0.8F,
          0.6F);
      mayStartSickness(vampire, victim, session, config, world.getRandom());
    }
    VillagerWitnesses.alert(world, vampire, victim, session, config.feeding().witnessRange(), bite);
    if (!victim.isAlive()) {
      return Result.DRAINED;
    }
    if (resists(victim, config, world.getRandom())) {
      breakFree(vampire, victim);
      return Result.RESISTED;
    }
    return Result.CONTINUE;
  }

  static void finish(
      final ServerPlayerEntity vampire,
      final LivingEntity victim,
      final FeedingSession session,
      final NightbornConfig config) {
    drinkFresh(vampire, session, session.heldForBottles(), config);
    session.heldForBottles(0.0F);
    if (victim != null && victim.isAlive() && session.totalTaken() > 0.0F) {
      final int seconds = config.feeding().sipWeaknessSeconds();
      if (seconds > 0) {
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, seconds * 20));
      }
    }
  }

  private static void keep(
      final ServerPlayerEntity vampire,
      final FeedingSession session,
      final float taken,
      final NightbornConfig config) {
    final ItemStack offHand = vampire.getOffHandStack();
    if (!offHand.isOf(Items.GLASS_BOTTLE)) {
      drinkFresh(vampire, session, taken, config);
      return;
    }
    final Bottling.Fill fill =
        Bottling.fill(
            session.heldForBottles() + taken, config.feeding().bottleSize(), offHand.getCount());
    session.heldForBottles(fill.leftover());
    for (int i = 0; i < fill.bottles(); i++) {
      offHand.decrement(1);
      final ItemStack bottle = BloodBottleItem.of(session.quality());
      if (!vampire.getInventory().insertStack(bottle)) {
        vampire.dropItem(bottle, false);
      }
    }
  }

  private static void drinkFresh(
      final ServerPlayerEntity vampire,
      final FeedingSession session,
      final float taken,
      final NightbornConfig config) {
    final float pending =
        session.pendingBlood()
            + BloodValue.worth(
                taken, session.quality().worthPercent(config), BloodValue.FRESH_PERCENT);
    final BloodValue.Split split = BloodValue.split(pending);
    session.pendingBlood(split.remainder());
    if (split.whole() > 0) {
      BloodAttachment.set(vampire, BloodAttachment.get(vampire) + split.whole());
    }
  }

  private static void mayStartSickness(
      final ServerPlayerEntity vampire,
      final LivingEntity victim,
      final FeedingSession session,
      final NightbornConfig config,
      final Random random) {
    if (victim instanceof ServerPlayerEntity human
        && !Vampires.isVampire(human)
        && config.turning().playersCanTurnPlayers()
        && session.rollTurnOnce()
        && random.nextInt(100) < config.turning().biteTurnChance()) {
      BiteSickness.start(human, config.turning());
    }
  }

  private static boolean resists(
      final LivingEntity victim, final NightbornConfig config, final Random random) {
    return random.nextFloat()
        < ResistChance.of(
            victim.getHealth(),
            victim.getMaxHealth(),
            config.feeding().resistChance(),
            victim.isSleeping());
  }

  private static void breakFree(final ServerPlayerEntity vampire, final LivingEntity victim) {
    vampire.takeKnockback(
        RESIST_PUSH, victim.getX() - vampire.getX(), victim.getZ() - vampire.getZ());
    vampire.velocityModified = true;
    if (victim instanceof MobEntity mob
        && mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
      mob.setTarget(vampire);
    }
  }
}
