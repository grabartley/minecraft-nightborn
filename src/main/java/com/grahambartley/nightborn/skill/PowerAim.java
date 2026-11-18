package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.mind.CommandAim;
import com.grahambartley.nightborn.mind.Commands;
import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.occult.HexPower;
import com.grahambartley.nightborn.occult.ShadowStep;
import com.grahambartley.nightborn.vanish.Vanish;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record PowerAim(
    @Nullable LivingEntity gazeTarget,
    @Nullable Vec3d stepTo,
    @Nullable LivingEntity target,
    @Nullable CommandAim command) {

  public static final PowerAim NONE = new PowerAim(null, null, null, null);

  public static boolean aims(final SkillPower power) {
    return power.mesmerise().isPresent()
        || power.shadowStep().isPresent()
        || power.vanish().isPresent()
        || power.pounce().isPresent()
        || power.hex().isPresent()
        || power.command().isPresent();
  }

  public static Optional<Float> targetRange(final SkillPower power) {
    return power.pounce().map(PouncePower::range).or(() -> power.hex().map(HexPower::range));
  }

  public static PowerAim of(
      final LivingEntity caster, final Optional<SkillPower> power, final PowerHeading heading) {
    if (power.isEmpty() || !aims(power.get())) {
      return NONE;
    }
    final SkillPower skill = power.get();
    return new PowerAim(
        skill
            .mesmerise()
            .map(mesmerise -> Mesmerise.target(caster, mesmerise.range()))
            .orElse(null),
        skill
            .shadowStep()
            .flatMap(step -> ShadowStep.destination(caster, step))
            .or(() -> skill.vanish().flatMap(vanish -> Vanish.destination(caster, vanish, heading)))
            .orElse(null),
        targetRange(skill).map(range -> PowerSight.aimed(caster, range)).orElse(null),
        skill.command().flatMap(command -> Commands.aim(caster, command)).orElse(null));
  }

  public boolean unseen(final SkillPower power) {
    return power.mesmerise().isPresent() && gazeTarget == null
        || targetRange(power).isPresent() && target == null;
  }

  public boolean nowhere(final SkillPower power) {
    return (power.shadowStep().isPresent() || power.vanish().isPresent()) && stepTo == null;
  }

  public boolean subjectless(final SkillPower power) {
    return power.command().isPresent() && command == null;
  }
}
