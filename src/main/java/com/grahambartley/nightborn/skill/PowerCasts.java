package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.lord.Rally;
import com.grahambartley.nightborn.lord.Summons;
import com.grahambartley.nightborn.mind.Commands;
import com.grahambartley.nightborn.mind.Dreads;
import com.grahambartley.nightborn.mind.Gazes;
import com.grahambartley.nightborn.mind.Glamour;
import com.grahambartley.nightborn.mind.MassGaze;
import com.grahambartley.nightborn.occult.Circles;
import com.grahambartley.nightborn.occult.FogClouds;
import com.grahambartley.nightborn.occult.GhostlyFamiliars;
import com.grahambartley.nightborn.occult.Hexes;
import com.grahambartley.nightborn.occult.MistForm;
import com.grahambartley.nightborn.occult.ShadowStep;
import com.grahambartley.nightborn.occult.Swarms;
import com.grahambartley.nightborn.sight.Sight;
import com.grahambartley.nightborn.sight.Sights;
import com.grahambartley.nightborn.vanish.Vanish;
import com.grahambartley.nightborn.village.SuspicionCause;
import com.grahambartley.nightborn.village.VampireSigns;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

final class PowerCasts {

  private PowerCasts() {}

  static void apply(
      final LivingEntity caster, final Identifier id, final SkillPower power, final PowerAim aim) {
    final long now = caster.getWorld().getTime();
    if (power.glamour().isEmpty()) {
      Glamour.end(caster);
    }
    for (final StatusEffectInstance effect : power.effects()) {
      caster.addStatusEffect(new StatusEffectInstance(effect), caster);
    }
    if (power.batForm().isEmpty() && power.wingedForm().isEmpty() && power.glamour().isEmpty()) {
      VampireSigns.shown(caster, SuspicionCause.SEEN_USING_A_POWER);
    }
    moves(caster, id, power, aim);
    minds(caster, power, aim);
    power.familiar().ifPresent(familiar -> GhostlyFamiliars.summon(caster, familiar));
    power.fog().ifPresent(fog -> FogClouds.spawn(caster, fog));
    power.rally().ifPresent(rally -> Rally.call(caster, rally));
    power.summon().ifPresent(summon -> Summons.call(caster, summon));
    power.circle().ifPresent(circle -> Circles.draw(caster, circle));
    power.swarm().ifPresent(swarm -> Swarms.start(caster, swarm));
    power.heal().ifPresent(heal -> Mending.start(caster, heal));
    power.frees().ifPresent(frees -> FreePowers.grant(caster, frees));
    power.boost().ifPresent(boost -> Boosts.start(caster, id, Boost.of(now, boost)));
    power
        .sense()
        .ifPresent(
            sense -> Sights.give(caster, Sight.sense(sense.range(), now + sense.durationTicks())));
    if (caster instanceof ServerPlayerEntity player) {
      power.track().ifPresent(track -> Tracking.start(player, track));
    }
    if (caster instanceof PowerShow show) {
      power.mob().flatMap(MobPowerUse::clip).ifPresent(show::showPower);
    }
    power
        .sound()
        .ifPresent(
            sound ->
                caster
                    .getWorld()
                    .playSound(
                        null,
                        caster.getBlockPos(),
                        SoundEvent.of(sound),
                        caster.getSoundCategory(),
                        1.0F,
                        1.0F));
  }

  private static void moves(
      final LivingEntity caster, final Identifier id, final SkillPower power, final PowerAim aim) {
    power.batForm().ifPresent(form -> BatForm.enter(caster));
    power.wingedForm().ifPresent(form -> WingedForm.enter(caster, id, form));
    power.mist().ifPresent(mist -> MistForm.enter(caster, mist));
    power.leap().ifPresent(leap -> leap.launch(caster, Powers.heading(caster)));
    if (aim.target() != null) {
      power.pounce().ifPresent(pounce -> Pounces.start(caster, aim.target(), pounce));
    }
    if (aim.stepTo() != null) {
      power
          .vanish()
          .ifPresentOrElse(
              vanish -> Vanish.go(caster, aim.stepTo(), vanish),
              () -> ShadowStep.step(caster, aim.stepTo()));
    }
  }

  private static void minds(final LivingEntity caster, final SkillPower power, final PowerAim aim) {
    if (aim.gazeTarget() != null) {
      power.mesmerise().ifPresent(mesmerise -> Gazes.start(caster, aim.gazeTarget(), mesmerise));
    }
    if (aim.target() != null) {
      power.hex().ifPresent(hex -> Hexes.cast(caster, aim.target(), hex));
    }
    if (aim.command() != null) {
      power.command().ifPresent(command -> Commands.give(caster, aim.command(), command));
    }
    power.massGaze().ifPresent(gaze -> MassGaze.cast(caster, gaze));
    power.dread().ifPresent(dread -> Dreads.start(caster, dread));
    power.glamour().ifPresent(glamour -> Glamour.start(caster, glamour));
  }
}
