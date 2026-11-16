package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.occult.ShadowStep;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record PowerAim(@Nullable LivingEntity gazeTarget, @Nullable Vec3d stepTo) {

  public static final PowerAim NONE = new PowerAim(null, null);

  public static PowerAim of(final LivingEntity caster, final Optional<SkillPower> power) {
    if (power.isEmpty()) {
      return NONE;
    }
    final SkillPower skill = power.get();
    return new PowerAim(
        skill
            .mesmerise()
            .map(mesmerise -> Mesmerise.target(caster, mesmerise.range()))
            .orElse(null),
        skill.shadowStep().flatMap(step -> ShadowStep.destination(caster, step)).orElse(null));
  }
}
