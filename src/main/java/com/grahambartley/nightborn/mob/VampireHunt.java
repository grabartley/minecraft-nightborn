package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.combat.Grudge;
import com.grahambartley.nightborn.mob.home.HomeLife;
import com.grahambartley.nightborn.mob.home.MobBlood;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;

public final class VampireHunt {

  private final MobBlood blood;
  private final HomeLife homeLife;
  private final Grudge grudge = new Grudge();

  public VampireHunt(final MobBlood blood, final HomeLife homeLife) {
    this.blood = blood;
    this.homeLife = homeLife;
  }

  public static boolean hunts(
      final boolean downed,
      final BooleanSupplier sleeping,
      final BooleanSupplier prey,
      final BooleanSupplier spared,
      final BooleanSupplier dropsPursuit) {
    return !downed
        && !sleeping.getAsBoolean()
        && prey.getAsBoolean()
        && !spared.getAsBoolean()
        && !dropsPursuit.getAsBoolean();
  }

  public static boolean pressed(final boolean provoked, final boolean onSight) {
    return provoked || onSight;
  }

  public static boolean letsGo(final boolean downed, final BooleanSupplier dropsPursuit) {
    return downed || dropsPursuit.getAsBoolean();
  }

  public boolean hunts(final VampireEntity vampire, final LivingEntity target) {
    return hunts(
        Downed.state(vampire) != null,
        vampire::isSleeping,
        () -> Prey.isPrey(target),
        () ->
            Prey.spared(
                vampire.getWorld().getDifficulty() == Difficulty.PEACEFUL,
                target instanceof PlayerEntity),
        () -> Hunting.dropsTarget(pursuit(vampire, target, false)));
  }

  public void tick(final VampireEntity vampire, final Runnable setOutToFeed) {
    final NightbornConfig config = ServerConfigService.current();
    final boolean downed = Downed.state(vampire) != null;
    grudge.update(vampire, downed);
    final LivingEntity target = vampire.getTarget();
    if (target != null
        && letsGo(
            downed,
            () ->
                Hunting.dropsTarget(
                    pursuit(
                        vampire,
                        target,
                        pressed(
                            Grudge.provoked(grudge.against(target), Prey.isPrey(target)),
                            vampire.attacksOnSight(target)))))) {
      grudge.forgive(target);
      vampire.setTarget(null);
    }
    homeLife.tick(vampire, blood.hungry(vampire, config), grudge.held(), config, setOutToFeed);
  }

  public void provokedBy(final LivingEntity attacker) {
    if (!Vampires.isVampire(attacker)) {
      grudge.provoke(attacker);
    }
  }

  public boolean bite(final VampireEntity vampire, final Entity target) {
    if (BatForm.isBat(vampire)
        || !(target instanceof LivingEntity victim)
        || !Prey.mayBite(
            Downed.state(vampire) != null,
            Vampires.isVampire(victim),
            Prey.isPrey(victim),
            victim == vampire.getTarget() || victim == vampire.getAttacker())) {
      return false;
    }
    vampire.hiss();
    final NightbornConfig config = ServerConfigService.current();
    final float before = victim.getHealth();
    final boolean bitten =
        MobBite.bite(
            vampire,
            victim,
            (float) vampire.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
            MobSounds.VAMPIRE_BITE);
    if (bitten) {
      vampire.onAttacking(victim);
      if (Prey.hasDrinkableBlood(victim)) {
        blood.drink(vampire, victim, BloodValue.taken(before, victim.getHealth()), config);
      }
    }
    return bitten;
  }

  private Hunting.Pursuit pursuit(
      final VampireEntity vampire, final LivingEntity target, final boolean provoked) {
    final NightbornConfig config = ServerConfigService.current();
    final boolean hungry = blood.hungry(vampire, config);
    return new Hunting.Pursuit(
        Hunting.vampireHunts(vampire.getWorld()),
        hungry,
        ShadeRules.endangered(vampire),
        provoked,
        homeLife.outOfRange(vampire, target, provoked, hungry, config));
  }
}
