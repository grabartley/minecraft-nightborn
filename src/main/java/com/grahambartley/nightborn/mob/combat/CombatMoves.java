package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import com.grahambartley.nightborn.mob.brain.Threat;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.slayer.Charges;
import com.grahambartley.nightborn.vampire.Impulse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class CombatMoves<T extends MobEntity & PowerShow> implements MobFighting.Moves {

  private final T fighter;
  private final Supplier<StageCombat> stage;
  private long nextMove;
  private boolean threatened;
  private boolean reacting;

  public CombatMoves(final T fighter, final Supplier<StageCombat> stage) {
    this.fighter = fighter;
    this.stage = stage;
  }

  @Override
  public void tick() {
    final LivingEntity target = fighter.getTarget();
    if (target == null) {
      threatened = false;
      reacting = false;
      return;
    }
    final boolean wasThreatened = threatened;
    threatened =
        Threat.of(target).at(fighter.distanceTo(target), ServerConfigService.current().combat());
    reacting =
        CombatMove.reacts(
            threatened, wasThreatened, () -> fighter.getRandom().nextInt(100), stage.get());
  }

  @Override
  public boolean reacting() {
    return reacting;
  }

  @Override
  public void reacted() {
    rested();
  }

  @Override
  public List<ActionReadiness> actions() {
    if (fighter.getTarget() == null) {
      return List.of();
    }
    final StageCombat combat = stage.get();
    final boolean free = fighter.isOnGround() && fighter.getWorld().getTime() >= nextMove;
    final int restTicks = restTicks(combat);
    final List<ActionReadiness> actions = new ArrayList<>(CombatMove.values().length);
    for (final CombatMove move : CombatMove.values()) {
      actions.add(move.readiness(free, () -> fighter.getRandom().nextInt(100), combat, restTicks));
    }
    return actions;
  }

  @Override
  public boolean act(final Identifier action) {
    final LivingEntity target = fighter.getTarget();
    if (target == null || !fighter.isOnGround()) {
      return false;
    }
    return CombatMove.byId(action)
        .map(
            move -> {
              move(move, target);
              return true;
            })
        .orElse(false);
  }

  private void move(final CombatMove move, final LivingEntity target) {
    final CombatSettings settings = ServerConfigService.current().combat();
    final Vec3d toward = target.getPos().subtract(fighter.getPos());
    fighter.setVelocity(
        fighter
            .getVelocity()
            .add(impulse(move.tuning(settings), move, toward, fighter.getRandom().nextBoolean())));
    fighter.velocityModified = true;
    fighter.showPower(move.clip());
    if (move != CombatMove.DODGE) {
      Charges.start(fighter);
    }
    fighter.playSound(SoundEvent.of(move.sound()), 1.0F, 1.0F);
    rested();
  }

  private void rested() {
    nextMove = fighter.getWorld().getTime() + restTicks(stage.get());
  }

  private static int restTicks(final StageCombat combat) {
    return combat.thinkTicks() * ServerConfigService.current().combat().moveCooldownThinks();
  }

  @Override
  public void rest() {
    threatened = false;
    reacting = false;
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
}
