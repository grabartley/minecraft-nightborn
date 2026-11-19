package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.faith.Kindling;
import com.grahambartley.nightborn.faith.Rebukes;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.moment.Moments;
import com.grahambartley.nightborn.network.UsePowerPayload;
import com.grahambartley.nightborn.seeker.Revelations;
import com.grahambartley.nightborn.seeker.SeekCasts;
import com.grahambartley.nightborn.slayer.Lunges;
import com.grahambartley.nightborn.slayer.QuickShots;
import com.grahambartley.nightborn.slayer.StakeThrows;
import com.grahambartley.nightborn.sun.DirectSun;
import com.grahambartley.nightborn.trap.Grounding;
import com.grahambartley.nightborn.trap.MarkedGrounds;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class Powers {

  private Powers() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        UsePowerPayload.ID,
        (payload, context) -> use(context.player(), payload.slot(), payload.heading()));
  }

  public static PowerUse.Verdict use(final ServerPlayerEntity player, final int slot) {
    return use(player, slot, PowerHeading.NONE);
  }

  public static PowerUse.Verdict use(
      final ServerPlayerEntity player, final int slot, final PowerHeading heading) {
    if (player.isSpectator() || !player.isAlive()) {
      return PowerUse.Verdict.NO_POWER;
    }
    final Registry<Skill> skills =
        Skills.skills(player.getServerWorld().getRegistryManager()).orElse(null);
    final PowerLayout bar = PowerBar.layout(skills, SkillAttachment.owned(player));
    final PowerUse.Verdict verdict = cast(player, bar.at(slot).orElse(null), heading);
    if (verdict != PowerUse.Verdict.READY && verdict != PowerUse.Verdict.NO_POWER) {
      player.sendMessage(Text.translatable(verdict.translationKey()), true);
    }
    return verdict;
  }

  public static Optional<SkillPower> power(
      final LivingEntity caster, @Nullable final Identifier id) {
    if (id == null || !SkillAttachment.owned(caster).owns(id)) {
      return Optional.empty();
    }
    return Skills.skills(caster.getWorld().getRegistryManager())
        .map(registry -> registry.get(id))
        .flatMap(Skill::power);
  }

  @Nullable
  public static LivingEntity foe(final LivingEntity caster) {
    return caster instanceof MobEntity mob ? mob.getTarget() : caster.getAttacking();
  }

  static Vec3d heading(final LivingEntity caster) {
    return caster instanceof MobEntity mob && mob.getTarget() != null
        ? mob.getTarget().getPos().subtract(caster.getPos())
        : caster.getRotationVector();
  }

  public static int cooldownTicks(
      final LivingEntity caster, @Nullable final Identifier id, final SkillPower power) {
    if (FreePowers.free(caster, id)) {
      return 0;
    }
    final List<PowerGift> gifts = SkillPowerGifts.forPower(caster, id);
    return PowerCooldowns.shortened(
        power.cooldownTicks(),
        PowerGifts.cooldown(
            VampireAttributes.value(caster, VampireAttributes.POWER_COOLDOWN),
            power.track().isPresent(),
            GiftAttributes.value(caster, GiftAttributes.TRACKING_COOLDOWN),
            PowerGifts.cooldownCut(gifts)));
  }

  public static int bloodCost(
      final LivingEntity caster, @Nullable final Identifier id, final SkillPower power) {
    if (FreePowers.free(caster, id)) {
      return 0;
    }
    return PowerGifts.blood(
        power.blood(),
        power.batForm().isPresent(),
        GiftAttributes.value(caster, GiftAttributes.BAT_FORM_COST),
        PowerGifts.bloodCut(SkillPowerGifts.forPower(caster, id)));
  }

  public static Optional<BatFormPower> ownedBatForm(final LivingEntity caster) {
    return SkillAttachment.owned(caster).ownedInOrder().stream()
        .map(id -> power(caster, id).flatMap(SkillPower::batForm))
        .flatMap(Optional::stream)
        .findFirst();
  }

  public static PowerUse.Verdict check(final LivingEntity caster, @Nullable final Identifier id) {
    return check(caster, id, power(caster, id));
  }

  public static PowerUse.Verdict check(
      final LivingEntity caster, @Nullable final Identifier id, final Optional<SkillPower> power) {
    return check(caster, id, power, PowerAim.of(caster, power, PowerHeading.NONE));
  }

  static PowerUse.Verdict check(
      final LivingEntity caster,
      @Nullable final Identifier id,
      final Optional<SkillPower> power,
      final PowerAim aim) {
    final boolean shifts =
        power
            .map(owned -> owned.batForm().isPresent() || owned.wingedForm().isPresent())
            .orElse(false);
    return PowerUse.check(
        new PowerUse.Situation(
            power.isPresent(),
            Downed.helpless(caster) || Moments.locked(caster),
            Holds.stopsPower(caster, power.map(owned -> owned.moves().count() > 0).orElse(false)),
            id != null && power.isPresent() && MarkedGrounds.bars(caster, id),
            power.map(SkillPower::leavesTheGround).orElse(false) && Grounding.on(caster),
            WingedForm.on(caster),
            BatForm.isBat(caster),
            Weakened.sources(caster).level(Downed.state(caster) != null) > 0,
            shifts && DirectSun.reaches(caster),
            id == null || FreePowers.free(caster, id)
                ? 0L
                : PowerCooldownAttachment.get(caster).ticksLeft(id, caster.getWorld().getTime()),
            BloodAttachment.get(caster),
            power.map(owned -> bloodCost(caster, id, owned)).orElse(0),
            power.map(aim::unseen).orElse(false),
            power.map(aim::nowhere).orElse(false),
            power.map(aim::unplaced).orElse(false),
            power.map(aim::subjectless).orElse(false),
            Rebukes.silenced(caster),
            power.flatMap(owned -> owned.faith().rebuke()).isPresent() && !Rebukes.armed(caster),
            power.map(aim::unarmed).orElse(false),
            power
                .flatMap(owned -> owned.faith().kindle())
                .map(kindle -> !Kindling.affords(caster, kindle))
                .orElse(false),
            power.map(owned -> aim.smothered(caster, owned)).orElse(false),
            power
                .map(
                    owned ->
                        owned.fight().stakeThrow().isPresent() && !StakeThrows.armed(caster)
                            || owned.lunge().isPresent() && !Lunges.armed(caster))
                .orElse(false),
            power
                .map(
                    owned ->
                        owned.fight().quickShot().isPresent()
                            && QuickShots.loaded(caster).isEmpty())
                .orElse(false),
            id != null && power.isPresent() && Revelations.bars(caster, id),
            power.map(owned -> SeekCasts.sceneless(caster, owned.seek())).orElse(false)));
  }

  public static PowerUse.Verdict cast(final LivingEntity caster, @Nullable final Identifier id) {
    return cast(caster, id, PowerHeading.NONE);
  }

  public static PowerUse.Verdict cast(
      final LivingEntity caster, @Nullable final Identifier id, final PowerHeading heading) {
    final Optional<SkillPower> owned = power(caster, id);
    if (BatForm.isBat(caster)
        && !WingedForm.on(caster)
        && owned.flatMap(SkillPower::batForm).isPresent()) {
      return BatForm.leave(caster) ? PowerUse.Verdict.READY : PowerUse.Verdict.CRAMPED;
    }
    final PowerAim aim = PowerAim.of(caster, owned, heading);
    final PowerUse.Verdict verdict = check(caster, id, owned, aim);
    if (verdict != PowerUse.Verdict.READY) {
      return verdict;
    }
    final SkillPower power = owned.orElseThrow();
    final int blood = bloodCost(caster, id, power);
    if (blood > 0) {
      BloodAttachment.set(caster, BloodAttachment.get(caster) - blood);
    }
    final long now = caster.getWorld().getTime();
    PowerCooldownAttachment.set(
        caster,
        PowerCooldownAttachment.get(caster).start(id, now, cooldownTicks(caster, id, power)));
    PowerCasts.apply(caster, id, power, aim);
    return verdict;
  }
}
