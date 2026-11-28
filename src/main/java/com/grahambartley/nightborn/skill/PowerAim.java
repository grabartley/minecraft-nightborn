package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faith.Wielders;
import com.grahambartley.nightborn.mind.CommandAim;
import com.grahambartley.nightborn.mind.Commands;
import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.occult.Circles;
import com.grahambartley.nightborn.occult.HexPower;
import com.grahambartley.nightborn.occult.ShadowStep;
import com.grahambartley.nightborn.seeker.MarkPower;
import com.grahambartley.nightborn.slayer.Dashes;
import com.grahambartley.nightborn.slayer.LungePower;
import com.grahambartley.nightborn.slayer.Lunges;
import com.grahambartley.nightborn.trap.TrapSpots;
import com.grahambartley.nightborn.vanish.Vanish;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record PowerAim(
    @Nullable LivingEntity gazeTarget,
    @Nullable Vec3d stepTo,
    @Nullable LivingEntity target,
    @Nullable CommandAim command,
    @Nullable BlockPos spot,
    @Nullable LivingEntity wielder) {

  public static final PowerAim NONE = new PowerAim(null, null, null, null, null, null);

  public static boolean aims(final SkillPower power) {
    return power.mesmerise().isPresent()
        || power.shadowStep().isPresent()
        || power.vanish().isPresent()
        || power.dash().isPresent()
        || power.lunge().isPresent()
        || power.pounce().isPresent()
        || power.hex().isPresent()
        || power.seek().markQuarry().isPresent()
        || power.command().isPresent()
        || places(power)
        || power.faith().armsAWeapon();
  }

  static boolean places(final SkillPower power) {
    return power.traps().places() || power.faith().spotRange().isPresent();
  }

  public static Optional<Float> targetRange(final SkillPower power) {
    return power
        .pounce()
        .map(PouncePower::range)
        .or(() -> power.hex().map(HexPower::range))
        .or(() -> power.lunge().map(LungePower::distance))
        .or(() -> power.seek().markQuarry().map(MarkPower::range));
  }

  public static PowerAim of(
      final LivingEntity caster, final Optional<SkillPower> power, final PowerHeading heading) {
    return of(caster, power, heading, CastAim.OWN_TARGET);
  }

  public static PowerAim of(
      final LivingEntity caster,
      final Optional<SkillPower> power,
      final PowerHeading heading,
      final CastAim cast) {
    if (power.isEmpty() || !aims(power.get())) {
      return NONE;
    }
    final SkillPower skill = power.get();
    final LivingEntity target =
        targetRange(skill).map(range -> PowerSight.aimed(caster, range, cast)).orElse(null);
    return new PowerAim(
        skill
            .mesmerise()
            .map(mesmerise -> Mesmerise.target(caster, mesmerise.range(), cast))
            .orElse(null),
        skill
            .shadowStep()
            .flatMap(step -> ShadowStep.destination(caster, step, cast))
            .or(
                () ->
                    skill
                        .vanish()
                        .flatMap(vanish -> Vanish.destination(caster, vanish, heading, cast)))
            .or(() -> skill.dash().flatMap(dash -> Dashes.destination(caster, dash, heading, cast)))
            .or(
                () ->
                    skill
                        .lunge()
                        .filter(lunge -> target != null)
                        .flatMap(lunge -> Lunges.destination(caster, target, lunge)))
            .orElse(null),
        target,
        skill.command().flatMap(command -> Commands.aim(caster, command)).orElse(null),
        skill
            .traps()
            .placeRange()
            .or(() -> skill.faith().spotRange())
            .flatMap(range -> TrapSpots.spot(caster, range, cast))
            .orElse(null),
        Wielders.of(caster, skill.faith(), cast));
  }

  public boolean unseen(final SkillPower power) {
    return power.mesmerise().isPresent() && gazeTarget == null
        || targetRange(power).isPresent() && target == null;
  }

  public boolean nowhere(final SkillPower power) {
    return (power.shadowStep().isPresent()
            || power.vanish().isPresent()
            || power.dash().isPresent()
            || power.lunge().isPresent())
        && stepTo == null;
  }

  public boolean unplaced(final SkillPower power) {
    return places(power) && spot == null;
  }

  public boolean smothered(final LivingEntity caster, final SkillPower power) {
    return power.faith().blessing().isPresent()
        && (Circles.stopsPrayers(caster.getWorld(), caster.getPos())
            || wielder != null && Circles.stopsPrayers(wielder.getWorld(), wielder.getPos()));
  }

  public boolean unarmed(final SkillPower power) {
    return power.faith().armsAWeapon() && wielder == null;
  }

  public boolean subjectless(final SkillPower power) {
    return power.command().isPresent() && command == null;
  }
}
