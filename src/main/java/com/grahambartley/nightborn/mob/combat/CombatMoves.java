package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.mob.brain.CastAims;
import com.grahambartley.nightborn.mob.brain.Thinking;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.slayer.Charges;
import com.grahambartley.nightborn.vampire.Impulse;
import java.util.function.Supplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.TridentItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

public final class CombatMoves<T extends MobEntity & PowerShow> implements MobFighting.Moves {

  private final T fighter;
  private final Supplier<StageCombat> stage;
  private long nextMove;
  private boolean threatened;

  public CombatMoves(final T fighter, final Supplier<StageCombat> stage) {
    this.fighter = fighter;
    this.stage = stage;
  }

  @Override
  public void tick() {
    final StageCombat combat = stage.get();
    final CombatSettings settings = ServerConfigService.current().combat();
    final LivingEntity target = fighter.getTarget();
    final long now = fighter.getWorld().getTime();
    if (target == null) {
      threatened = false;
      return;
    }
    final double distance = fighter.distanceTo(target);
    final CombatMove.Threat threat = new CombatMove.Threat(target.handSwinging, draws(target));
    final boolean wasThreatened = threatened;
    threatened = threat.at(distance, settings);
    if (CombatMove.dodgesWithPower(
            threatened, wasThreatened, () -> fighter.getRandom().nextInt(100), combat)
        && dodgeWithPower(fighter, combat)) {
      nextMove = now + (long) combat.thinkTicks() * settings.moveCooldownThinks();
      return;
    }
    if (now < nextMove || !fighter.isOnGround()) {
      return;
    }
    nextMove = now + combat.thinkTicks();
    CombatMove.decide(distance, true, threat, fighter.getRandom().nextInt(100), combat, settings)
        .ifPresent(
            move -> {
              final Vec3d toward = target.getPos().subtract(fighter.getPos());
              fighter.setVelocity(
                  fighter
                      .getVelocity()
                      .add(
                          impulse(
                              move.tuning(settings),
                              move,
                              toward,
                              fighter.getRandom().nextBoolean())));
              fighter.velocityModified = true;
              fighter.showPower(move.clip());
              if (move != CombatMove.DODGE) {
                Charges.start(fighter);
              }
              fighter.playSound(SoundEvent.of(move.sound()), 1.0F, 1.0F);
              nextMove = now + (long) combat.thinkTicks() * settings.moveCooldownThinks();
            });
  }

  private static boolean dodgeWithPower(final MobEntity fighter, final StageCombat combat) {
    return Thinking.think(fighter, true, combat.bloodFloor())
        .flatMap(think -> think.chosen())
        .flatMap(
            scored ->
                CastAims.resolve(fighter, scored.use().aimed())
                    .map(
                        aim ->
                            Powers.cast(fighter, scored.option().power(), aim)
                                == PowerUse.Verdict.READY))
        .orElse(false);
  }

  @Override
  public void rest() {
    threatened = false;
  }

  public static Vec3d impulse(
      final CombatMove.Tuning tuning,
      final CombatMove move,
      final Vec3d toward,
      final boolean left) {
    final Vec3d ahead = Impulse.ahead(toward);
    final Vec3d heading =
        move == CombatMove.DODGE
            ? (left ? new Vec3d(-ahead.z, 0.0, ahead.x) : new Vec3d(ahead.z, 0.0, -ahead.x))
            : ahead;
    return Impulse.push(heading, tuning.pushBlocks(), tuning.liftBlocks());
  }

  public static boolean draws(final LivingEntity target) {
    return target.isUsingItem() && ranged(target.getActiveItem());
  }

  static boolean ranged(final ItemStack stack) {
    return stack.getItem() instanceof RangedWeaponItem || stack.getItem() instanceof TridentItem;
  }
}
