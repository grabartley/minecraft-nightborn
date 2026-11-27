package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.brain.PowerChoice;
import com.grahambartley.nightborn.mob.brain.Think;
import com.grahambartley.nightborn.mob.brain.Thinking;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PowerCasting {

  private final BiFunction<LivingEntity, Identifier, PowerUse.Verdict> cast;
  @Nullable private Tell telling;
  @Nullable private Think lastThink;
  private long nextThink;

  public PowerCasting() {
    this(Powers::cast);
  }

  private PowerCasting(final BiFunction<LivingEntity, Identifier, PowerUse.Verdict> cast) {
    this.cast = cast;
  }

  public static PowerCasting underTell() {
    return new PowerCasting(Powers::castUnderOwnClip);
  }

  public static boolean thinks(final boolean hasTarget, final long now, final long nextThink) {
    return hasTarget && now >= nextThink;
  }

  public <T extends MobEntity & PowerShow> void tick(
      final T caster, final int thinkTicks, final int bloodFloor) {
    final long now = caster.getWorld().getTime();
    final LivingEntity target = caster.getTarget();
    if (telling != null) {
      final Tell.Step step = telling.step(target != null, now);
      if (step == Tell.Step.CAST) {
        cast.apply(caster, telling.power());
      }
      if (step != Tell.Step.WAIT) {
        telling = null;
      }
      return;
    }
    if (!thinks(target != null, now, nextThink)) {
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
                    tell(caster, scored.option(), now);
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
      final T caster, final PowerChoice.Option option, final long now) {
    final MobPowerUse use = option.use();
    telling = new Tell(option.power(), now + use.tellTicks());
    caster.showPower(use.tellClip());
    caster.playSound(SoundEvent.of(use.tellSound()), 1.0F, 1.0F);
  }
}
