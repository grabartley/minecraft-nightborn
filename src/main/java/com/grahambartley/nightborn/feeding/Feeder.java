package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.network.PlayerCue;
import com.grahambartley.nightborn.network.PlayerCues;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.sound.NightbornSounds;
import com.grahambartley.nightborn.thrall.Thralls;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.VampireAttributes;
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
            victim.getHealth(),
            config.feeding().drinkPerSecond()
                * (float) VampireAttributes.value(vampire, VampireAttributes.FEEDING_SPEED),
            session.atLastHealth());
    session.atLastHealth(sip.atLastHealth());
    final DamageSource bite = NightbornDamage.bite(world, vampire);
    float taken = 0.0F;
    if (sip.take() > 0.0F) {
      final float before = victim.getHealth();
      victim.damage(bite, sip.drains() ? sip.take() + victim.getAbsorptionAmount() : sip.take());
      taken = BloodValue.taken(before, victim.getHealth());
      session.took(taken);
      if (taken > 0.0F) {
        MilestoneCriterion.reach(victim, Milestone.BITTEN);
        MilestoneCriterion.reach(vampire, Milestone.FED, victim);
      }
      final float worth =
          BloodValue.worth(taken, session.quality().worthPercent(config), BloodValue.FRESH_PERCENT);
      SkillEarning.fromBlood(vampire, worth, config.skills());
      keep(vampire, session, taken, worth, config);
      world.playSound(
          null,
          vampire.getBlockPos(),
          NightbornSounds.sip(sip.drains()),
          SoundCategory.PLAYERS,
          0.8F,
          NightbornSounds.variedPitch(world.getRandom()));
      mayStartSickness(vampire, victim, session, config, world.getRandom());
    }
    if (VillagerWitnesses.alert(
            world, vampire, victim, session, config.feeding().witnessRange(), bite)
        && taken > 0.0F) {
      MilestoneCriterion.reach(vampire, Milestone.FED_WATCHED);
    }
    if (!victim.isAlive()) {
      if (sip.drains()) {
        SkillEarning.fromDrain(vampire, config.skills());
        PlayerCues.send(vampire, PlayerCue.DRAIN);
      }
      return Result.DRAINED;
    }
    if (resists(vampire, victim, config, world.getRandom())) {
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
    drinkFresh(
        vampire,
        session,
        BloodValue.worth(
            session.heldForBottles(),
            session.quality().worthPercent(config),
            BloodValue.FRESH_PERCENT));
    session.heldForBottles(0.0F);
    if (victim != null && victim.isAlive() && session.totalTaken() > 0.0F) {
      final int seconds = config.feeding().sipWeaknessSeconds();
      if (seconds > 0) {
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, seconds * 20));
      }
      Thralls.mayBind(vampire, victim, session.totalTaken());
    }
  }

  private static void keep(
      final ServerPlayerEntity vampire,
      final FeedingSession session,
      final float taken,
      final float worth,
      final NightbornConfig config) {
    final ItemStack offHand = vampire.getOffHandStack();
    if (!offHand.isOf(Items.GLASS_BOTTLE)) {
      drinkFresh(vampire, session, worth);
      return;
    }
    final Bottling.Fill fill =
        Bottling.fill(
            session.heldForBottles() + taken, config.feeding().bottleSize(), offHand.getCount());
    session.heldForBottles(fill.leftover());
    for (int i = 0; i < fill.bottles(); i++) {
      offHand.decrement(1);
      final ItemStack bottle = BloodBottleItem.of(session.quality());
      if (!BottleStowing.stow(vampire.getInventory(), bottle)) {
        vampire.dropItem(bottle, false);
      }
    }
  }

  private static void drinkFresh(
      final ServerPlayerEntity vampire, final FeedingSession session, final float worth) {
    final float pending = session.pendingBlood() + worth;
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
    BiteSickness.startFromBite(
        victim,
        config.turning().biteTurnChance(),
        TurningBite.isOn(vampire),
        config.turning().playersCanTurnPlayers(),
        session.rollTurnOnce(),
        random,
        config.turning());
  }

  private static boolean resists(
      final LivingEntity vampire,
      final LivingEntity victim,
      final NightbornConfig config,
      final Random random) {
    return random.nextFloat()
        < ResistChance.held(
            ResistChance.of(
                victim.getHealth(),
                victim.getMaxHealth(),
                config.feeding().resistChance(),
                victim.isSleeping() || MindTies.mesmerised(victim)),
            VampireAttributes.value(vampire, VampireAttributes.VICTIM_STRUGGLE));
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
