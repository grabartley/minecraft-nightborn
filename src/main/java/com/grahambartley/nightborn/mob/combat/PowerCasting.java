package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.brain.Aim;
import com.grahambartley.nightborn.mob.brain.CastAims;
import com.grahambartley.nightborn.mob.brain.FormHold;
import com.grahambartley.nightborn.mob.brain.HelpClaims;
import com.grahambartley.nightborn.mob.brain.Mind;
import com.grahambartley.nightborn.mob.brain.PowerChoice;
import com.grahambartley.nightborn.mob.brain.RouteEnd;
import com.grahambartley.nightborn.mob.brain.TargetMotion;
import com.grahambartley.nightborn.mob.brain.Think;
import com.grahambartley.nightborn.mob.brain.Thinking;
import com.grahambartley.nightborn.skill.CastAim;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillPower;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class PowerCasting {

  @FunctionalInterface
  interface Cast {
    PowerUse.Verdict apply(LivingEntity caster, Identifier power, CastAim aim);
  }

  private final Cast cast;
  private final TargetMotion motion = new TargetMotion();
  private final FormHold hold = new FormHold();
  @Nullable private Tell telling;
  @Nullable private Think lastThink;
  private long nextThink;
  private long nextAid;

  public PowerCasting() {
    this(Powers::cast);
  }

  private PowerCasting(final Cast cast) {
    this.cast = cast;
  }

  public static PowerCasting underTell() {
    return new PowerCasting(Powers::castUnderOwnClip);
  }

  public static boolean thinks(
      final boolean engaged, final long now, final long nextThink, final long nextAid) {
    return now >= (engaged ? nextThink : nextAid);
  }

  public <T extends MobEntity & PowerShow> void tick(
      final T caster,
      final boolean engaged,
      final Mind mind,
      final int bloodFloor,
      final MobFighting.Moves moves) {
    final int thinkTicks = mind.thinkTicks();
    final long now = caster.getWorld().getTime();
    if (telling != null) {
      final Tell.Step step = telling.step(engaged, now);
      if (step == Tell.Step.CAST) {
        final Identifier power = telling.power();
        final Optional<CastAim> aim =
            CastAims.resolve(caster, telling.aim(), aided(caster, telling.ally()));
        if (aim.isPresent()) {
          final int holdTicks = holdTicks(caster, power);
          final Vec3d course = holdTicks > 0 ? RouteEnd.course(caster) : null;
          if (cast.apply(caster, power, aim.get()) == PowerUse.Verdict.READY && holdTicks > 0) {
            hold.take(now, holdTicks, course);
          }
        } else {
          nextThink = now;
        }
      }
      if (step != Tell.Step.WAIT) {
        telling = null;
      }
      return;
    }
    if (!thinks(engaged, now, nextThink, nextAid)) {
      return;
    }
    if (engaged) {
      nextThink = now + thinkTicks;
    }
    final Optional<Think> think = Thinking.think(caster, bloodFloor, motion, mind, moves.actions());
    if (!engaged) {
      nextAid = now + Thinking.idleTicks(caster, think, thinkTicks);
    }
    think.ifPresent(
        thought -> {
          lastThink = thought;
          thought
              .chosen()
              .ifPresent(
                  scored -> {
                    if (scored.option().action()
                        ? !moves.act(scored.option().id())
                        : !tellable(scored.use().aimed() == Aim.ALLY, allyVerdict(caster, scored))
                            || !tell(caster, scored, now, engaged)) {
                      return;
                    }
                    nextThink =
                        Math.max(nextThink, now + thinkTicks) + thought.budget().cooldownPrice();
                    nextAid = nextThink;
                  });
        });
  }

  public <T extends MobEntity & PowerShow> boolean react(
      final T caster, final int bloodFloor, final Mind mind) {
    final Optional<Think> think = Thinking.react(caster, bloodFloor, mind);
    final Optional<PowerChoice.Scored> chosen = think.flatMap(Think::chosen);
    if (chosen.isEmpty()) {
      return false;
    }
    lastThink = think.get();
    final PowerChoice.Scored scored = chosen.get();
    return CastAims.resolve(caster, scored.use().aimed())
        .map(aim -> cast.apply(caster, scored.option().id(), aim) == PowerUse.Verdict.READY)
        .orElse(false);
  }

  private static int holdTicks(final MobEntity caster, final Identifier power) {
    return Powers.power(caster, power)
        .flatMap(SkillPower::mob)
        .map(MobPowerUse::holdTicks)
        .orElse(0);
  }

  public static boolean tellable(
      final boolean aimsAtAlly, @Nullable final PowerUse.Verdict allyVerdict) {
    return !aimsAtAlly || allyVerdict == PowerUse.Verdict.READY;
  }

  @Nullable
  private static PowerUse.Verdict allyVerdict(
      final MobEntity caster, final PowerChoice.Scored scored) {
    final LivingEntity ally = aided(caster, scored.ally() == null ? null : scored.ally().id());
    if (ally == null) {
      return null;
    }
    final Identifier power = scored.option().id();
    return Powers.check(caster, power, Powers.power(caster, power), CastAim.at(ally));
  }

  @Nullable
  private static LivingEntity aided(final MobEntity caster, @Nullable final UUID ally) {
    return ally != null
            && caster.getWorld() instanceof ServerWorld world
            && world.getEntity(ally) instanceof LivingEntity living
            && living.isAlive()
        ? living
        : null;
  }

  public void cancel() {
    telling = null;
  }

  public boolean telling() {
    return telling != null;
  }

  public Optional<Think> lastThink() {
    return Optional.ofNullable(lastThink);
  }

  public FormHold hold() {
    return hold;
  }

  private <T extends MobEntity & PowerShow> boolean tell(
      final T caster, final PowerChoice.Scored scored, final long now, final boolean inFight) {
    final Identifier power = scored.option().id();
    final MobPowerUse use = Powers.power(caster, power).flatMap(SkillPower::mob).orElse(null);
    if (use == null) {
      return false;
    }
    final long castAt = now + use.tellTicks();
    final UUID ally = scored.ally() == null ? null : scored.ally().id();
    telling = new Tell(power, castAt, scored.use().aimed(), ally, inFight);
    if (ally != null) {
      HelpClaims.claim(caster.getWorld(), ally, caster.getUuid(), castAt);
    }
    caster.showPower(use.tellClip());
    caster.playSound(SoundEvent.of(use.tellSound()), 1.0F, 1.0F);
    return true;
  }
}
