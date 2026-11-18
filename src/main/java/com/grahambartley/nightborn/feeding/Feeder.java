package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.accessory.GiftScale;
import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.age.AgeDeeds;
import com.grahambartley.nightborn.age.Ageing;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.blood.BloodGift;
import com.grahambartley.nightborn.blood.BloodGiftGrants;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.network.PlayerCue;
import com.grahambartley.nightborn.network.PlayerCues;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.sound.NightbornSounds;
import com.grahambartley.nightborn.thrall.Thralls;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.village.VillageSuspicion;
import com.grahambartley.nightborn.village.VillageVictims;
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
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

final class Feeder {

  static final double RESIST_PUSH = 0.8;
  static final String TOO_LITTLE_BLOOD = "nightborn.turning_bite.too_little_blood";

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
        if (session.totalTaken() == taken) {
          Deeds.record(victim, Deed.of(DeedKinds.BITTEN));
        }
      }
      final float worth =
          BloodValue.worth(taken, session.quality().worthPercent(config), BloodValue.FRESH_PERCENT);
      SkillEarning.fromBlood(vampire, worth, config.skills());
      keep(
          vampire,
          session,
          taken,
          BiteGifts.feeding(worth, GiftAttributes.value(vampire, GiftAttributes.FEEDING_BLOOD)),
          config);
      world.playSound(
          null,
          vampire.getBlockPos(),
          NightbornSounds.sip(sip.drains()),
          SoundCategory.PLAYERS,
          0.8F,
          NightbornSounds.variedPitch(world.getRandom()));
      mayStartSickness(vampire, victim, session, config, world.getRandom());
    }
    final boolean villagersSaw =
        VillagerWitnesses.alert(
            world, vampire, victim, session, config.feeding().witnessRange(), bite);
    if (villagersSaw && taken > 0.0F) {
      MilestoneCriterion.reach(vampire, Milestone.FED_WATCHED);
    }
    if (taken > 0.0F
        && !session.seen()
        && (villagersSaw
            || FeedingWitnesses.anySees(world, vampire, victim, config.feeding().witnessRange()))) {
      session.markSeen();
    }
    if (!victim.isAlive()) {
      if (sip.drains()) {
        VillageVictims.bitten(world, victim);
        SkillEarning.fromDrain(vampire, config.skills());
        AgeDeeds.drained(vampire, victim);
        PlayerCues.send(vampire, PlayerCue.DRAIN);
      }
      return Result.DRAINED;
    }
    if (BiteQuarry.struggles(victim) && resists(vampire, victim, config, world.getRandom())) {
      breakFree(vampire, victim);
      return Result.RESISTED;
    }
    return Result.CONTINUE;
  }

  static void finishOff(
      final ServerWorld world,
      final ServerPlayerEntity vampire,
      final LivingEntity victim,
      final FeedingSession session,
      final NightbornConfig config) {
    final float before = victim.getHealth();
    victim.damage(NightbornDamage.bite(world, vampire), before + victim.getAbsorptionAmount());
    final float taken = BloodValue.taken(before, victim.getHealth());
    session.took(taken);
    if (taken > 0.0F) {
      MilestoneCriterion.reach(victim, Milestone.BITTEN);
      MilestoneCriterion.reach(vampire, Milestone.FED, victim);
      if (session.totalTaken() == taken) {
        Deeds.record(victim, Deed.of(DeedKinds.BITTEN));
      }
    }
    if (taken > 0.0F
        && !session.seen()
        && (!VillagerWitnesses.witnesses(world, vampire, victim, config.feeding().witnessRange())
                .isEmpty()
            || FeedingWitnesses.anySees(world, vampire, victim, config.feeding().witnessRange()))) {
      session.markSeen();
    }
    final float worth =
        BloodValue.worth(taken, session.quality().worthPercent(config), BloodValue.FRESH_PERCENT);
    SkillEarning.fromBlood(vampire, worth, config.skills());
    drinkFresh(
        vampire,
        session,
        BiteGifts.finisher(worth, GiftAttributes.value(vampire, GiftAttributes.FINISHER_BLOOD)));
    world.playSound(
        null,
        vampire.getBlockPos(),
        NightbornSounds.sip(true),
        SoundCategory.PLAYERS,
        0.8F,
        NightbornSounds.variedPitch(world.getRandom()));
    if (!victim.isAlive()) {
      VillageVictims.bitten(world, victim);
      vampire.heal(config.feeding().finisherHeal());
      SkillEarning.fromDrain(vampire, config.skills());
      AgeDeeds.drained(vampire, victim);
      Deeds.record(vampire, Deed.of(DeedKinds.FINISHER_KILL));
    }
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
      final int ticks =
          GiftScale.ticks(
              BiteResistance.weaknessSeconds(
                      config.feeding().sipWeaknessSeconds(), biteResistance(victim))
                  * 20,
              GiftAttributes.value(victim, GiftAttributes.BITE_WEAKNESS_TIME));
      if (ticks > 0) {
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, ticks));
      }
      VillageVictims.bitten(vampire.getServerWorld(), victim);
      Thralls.mayBind(vampire, victim, session.totalTaken());
    }
    if (victim != null && session.totalTaken() > 0.0F) {
      Deeds.record(
          vampire,
          FeedDeed.of(
              victim,
              session.seen(),
              session.gift().filter(gift -> session.giftTaken()),
              VillageSuspicion.warded(vampire.getServerWorld(), victim.getBlockPos())));
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
      final ItemStack bottle = BloodBottleItem.of(session.quality(), session.gift());
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
      final int before = BloodAttachment.get(vampire);
      BloodAttachment.set(vampire, before + split.whole());
      Ageing.drank(vampire, BloodAttachment.get(vampire) - before);
    }
    if (worth > 0.0F && session.gift().isPresent()) {
      BloodGiftGrants.grant(vampire, session.gift().get(), BloodGift.FRESH);
      session.markGiftTaken();
    }
  }

  private static void mayStartSickness(
      final ServerPlayerEntity vampire,
      final LivingEntity victim,
      final FeedingSession session,
      final NightbornConfig config,
      final Random random) {
    if (!session.rollTurnOnce()) {
      return;
    }
    final int cost =
        BiteRules.turningCost(BloodCapacity.of(vampire), config.turning().turningBiteBloodCost());
    final BiteRules.Turning turning =
        BiteRules.turning(
            TurningBite.isOn(vampire),
            victim instanceof ServerPlayerEntity && !Vampires.isVampire(victim),
            config.turning().playersCanTurnPlayers(),
            BloodAttachment.get(vampire),
            cost);
    if (turning == BiteRules.Turning.TOO_LITTLE_BLOOD) {
      vampire.sendMessage(Text.translatable(TOO_LITTLE_BLOOD), true);
    }
    if (turning == BiteRules.Turning.MAY_TURN
        && BiteSickness.startFromTurningBite(victim, random, config.turning())) {
      BloodAttachment.set(vampire, BloodAttachment.get(vampire) - cost);
    }
  }

  private static boolean resists(
      final LivingEntity vampire,
      final LivingEntity victim,
      final NightbornConfig config,
      final Random random) {
    return random.nextFloat()
        < BiteResistance.breakFreeChance(
                ResistChance.held(
                    ResistChance.of(
                        victim.getHealth(),
                        victim.getMaxHealth(),
                        config.feeding().resistChance(),
                        victim.isSleeping() || MindTies.mesmerised(victim)),
                    VampireAttributes.value(vampire, VampireAttributes.VICTIM_STRUGGLE)),
                biteResistance(victim))
            * (float) GiftAttributes.value(victim, GiftAttributes.BITE_ESCAPE);
  }

  private static double biteResistance(final LivingEntity victim) {
    return victim.getAttributes().hasAttribute(HunterAttributes.BITE_RESISTANCE)
        ? victim.getAttributeValue(HunterAttributes.BITE_RESISTANCE)
        : 0.0;
  }

  private static void breakFree(final ServerPlayerEntity vampire, final LivingEntity victim) {
    Deeds.record(victim, Deed.of(DeedKinds.BITE_BROKEN));
    vampire.takeKnockback(
        RESIST_PUSH, victim.getX() - vampire.getX(), victim.getZ() - vampire.getZ());
    vampire.velocityModified = true;
    if (victim instanceof MobEntity mob
        && mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
      mob.setTarget(vampire);
    }
  }
}
