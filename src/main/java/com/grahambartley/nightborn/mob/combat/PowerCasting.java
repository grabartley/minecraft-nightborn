package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.brain.PowerChoice;
import com.grahambartley.nightborn.mob.brain.Think;
import com.grahambartley.nightborn.mob.brain.Thinking;
import com.grahambartley.nightborn.skill.CastAim;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PowerCasting {

  @FunctionalInterface
  interface Cast {
    PowerUse.Verdict apply(LivingEntity caster, Identifier power, CastAim aim);
  }

  private final Cast cast;
  @Nullable private Tell telling;
  @Nullable private Think lastThink;
  private long nextThink;

  public PowerCasting() {
    this(Powers::cast);
  }

  private PowerCasting(final Cast cast) {
    this.cast = cast;
  }

  public static PowerCasting underTell() {
    return new PowerCasting(Powers::castUnderOwnClip);
  }

  public static boolean thinks(final boolean engaged, final long now, final long nextThink) {
    return engaged && now >= nextThink;
  }

  public <T extends MobEntity & PowerShow> void tick(
      final T caster, final boolean engaged, final int thinkTicks, final int bloodFloor) {
    final long now = caster.getWorld().getTime();
    if (telling != null) {
      final Tell.Step step = telling.step(engaged, now);
      if (step == Tell.Step.CAST) {
        final Identifier power = telling.power();
        CastAims.resolve(caster, telling.aim()).ifPresent(aim -> cast.apply(caster, power, aim));
      }
      if (step != Tell.Step.WAIT) {
        telling = null;
      }
      return;
    }
    if (!thinks(engaged, now, nextThink)) {
      return;
    }
    nextThink = now + thinkTicks;
    final Optional<Think> think = Thinking.think(caster, false, bloodFloor);
    think.ifPresent(
        thought -> {
          lastThink = thought;
          thought
              .chosen()
              .ifPresent(
                  scored -> {
                    nextThink += thought.budget().cooldownPrice();
                    tell(caster, scored, now);
                  });
        });
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

  private <T extends MobEntity & PowerShow> void tell(
      final T caster, final PowerChoice.Scored scored, final long now) {
    final MobPowerUse use = scored.option().use();
    telling = new Tell(scored.option().power(), now + use.tellTicks(), scored.use().aimed());
    caster.showPower(use.tellClip());
    caster.playSound(SoundEvent.of(use.tellSound()), 1.0F, 1.0F);
  }
}
