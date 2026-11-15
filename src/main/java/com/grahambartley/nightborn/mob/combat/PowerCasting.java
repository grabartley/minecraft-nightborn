package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillPower;
import com.grahambartley.nightborn.skill.Skills;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PowerCasting {

  @Nullable private Identifier telling;
  private long castAt;
  private long nextThink;

  public <T extends MobEntity & PowerShow> void tick(final T caster, final StageCombat combat) {
    final long now = caster.getWorld().getTime();
    final LivingEntity target = caster.getTarget();
    if (telling != null) {
      if (target == null) {
        telling = null;
      } else if (now >= castAt) {
        Powers.cast(caster, telling);
        telling = null;
      }
      return;
    }
    if (target == null || now < nextThink) {
      return;
    }
    nextThink = now + combat.thinkTicks();
    PowerChoice.choose(
            options(caster),
            caster.distanceTo(target),
            BloodAttachment.get(caster),
            combat.bloodFloor())
        .ifPresent(option -> tell(caster, option, now));
  }

  public boolean telling() {
    return telling != null;
  }

  private <T extends MobEntity & PowerShow> void tell(
      final T caster, final PowerChoice.Option option, final long now) {
    final MobPowerUse use = option.use();
    telling = option.power();
    castAt = now + use.tellTicks();
    caster.showPower(use.tellClip());
    caster.playSound(SoundEvent.of(use.tellSound()), 1.0F, 1.0F);
  }

  static List<PowerChoice.Option> options(final LivingEntity caster) {
    final Registry<Skill> skills =
        Skills.skills(caster.getWorld().getRegistryManager()).orElse(null);
    final List<PowerChoice.Option> options = new ArrayList<>();
    if (skills == null) {
      return options;
    }
    for (final Identifier id : SkillAttachment.owned(caster).ownedInOrder()) {
      final Skill skill = skills.get(id);
      final SkillPower power = skill == null ? null : skill.power().orElse(null);
      if (power != null && power.mob().isPresent()) {
        options.add(
            new PowerChoice.Option(id, Powers.check(caster, id), power.blood(), power.mob().get()));
      }
    }
    return options;
  }
}
