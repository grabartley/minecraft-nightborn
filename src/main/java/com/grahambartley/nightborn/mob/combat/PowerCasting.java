package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PowerCasting {

  @Nullable private Tell telling;
  private long nextThink;

  public <T extends MobEntity & PowerShow> void tick(final T caster, final StageCombat combat) {
    final long now = caster.getWorld().getTime();
    final LivingEntity target = caster.getTarget();
    if (telling != null) {
      final Tell.Step step = telling.step(target != null, now);
      if (step == Tell.Step.CAST) {
        Powers.cast(caster, telling.power());
      }
      if (step != Tell.Step.WAIT) {
        telling = null;
      }
      return;
    }
    if (target == null || now < nextThink) {
      return;
    }
    nextThink = now + combat.thinkTicks();
    PowerChoice.choose(
            options(caster, false),
            caster.distanceTo(target),
            BloodAttachment.get(caster),
            combat.bloodFloor())
        .ifPresent(option -> tell(caster, option, now));
  }

  public void cancel() {
    telling = null;
  }

  public boolean telling() {
    return telling != null;
  }

  private <T extends MobEntity & PowerShow> void tell(
      final T caster, final PowerChoice.Option option, final long now) {
    final MobPowerUse use = option.use();
    telling = new Tell(option.power(), now + use.tellTicks());
    caster.showPower(use.tellClip());
    caster.playSound(SoundEvent.of(use.tellSound()), 1.0F, 1.0F);
  }

  static List<PowerChoice.Option> options(final LivingEntity caster, final boolean dodge) {
    final List<PowerChoice.Option> options = new ArrayList<>();
    for (final Identifier id : SkillAttachment.owned(caster).ownedInOrder()) {
      Powers.power(caster, id)
          .filter(power -> power.mob().map(use -> use.dodge() == dodge).orElse(false))
          .ifPresent(
              power ->
                  options.add(
                      new PowerChoice.Option(
                          id,
                          Powers.check(caster, id, Optional.of(power)),
                          power.blood(),
                          power.mob().get())));
    }
    return options;
  }
}
