package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.network.UsePowerPayload;
import com.grahambartley.nightborn.sun.DirectSun;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class Powers {

  private Powers() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        UsePowerPayload.ID, (payload, context) -> use(context.player(), payload.slot()));
  }

  public static PowerUse.Verdict use(final ServerPlayerEntity player, final int slot) {
    if (player.isSpectator() || !player.isAlive()) {
      return PowerUse.Verdict.NO_POWER;
    }
    final Registry<Skill> skills =
        Skills.skills(player.getServerWorld().getRegistryManager()).orElse(null);
    final List<Identifier> bar = PowerBar.slots(skills, SkillAttachment.owned(player));
    final PowerUse.Verdict verdict =
        cast(player, slot >= 0 && slot < bar.size() ? bar.get(slot) : null);
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

  static Vec3d heading(final LivingEntity caster) {
    return caster instanceof MobEntity mob && mob.getTarget() != null
        ? mob.getTarget().getPos().subtract(caster.getPos())
        : caster.getRotationVector();
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
    return PowerUse.check(
        power.isPresent(),
        Downed.helpless(caster),
        BatForm.isBat(caster),
        Weakened.sources(caster).level(Downed.state(caster) != null) > 0,
        power.flatMap(SkillPower::batForm).isPresent() && DirectSun.on(caster),
        id == null
            ? 0L
            : PowerCooldownAttachment.get(caster).ticksLeft(id, caster.getWorld().getTime()),
        BloodAttachment.get(caster),
        power.map(SkillPower::blood).orElse(0));
  }

  public static PowerUse.Verdict cast(final LivingEntity caster, @Nullable final Identifier id) {
    final Optional<SkillPower> owned = power(caster, id);
    if (BatForm.isBat(caster) && owned.flatMap(SkillPower::batForm).isPresent()) {
      return BatForm.leave(caster) ? PowerUse.Verdict.READY : PowerUse.Verdict.CRAMPED;
    }
    final PowerUse.Verdict verdict = check(caster, id, owned);
    if (verdict != PowerUse.Verdict.READY) {
      return verdict;
    }
    final SkillPower power = owned.orElseThrow();
    if (power.blood() > 0) {
      BloodAttachment.set(caster, BloodAttachment.get(caster) - power.blood());
    }
    final long now = caster.getWorld().getTime();
    PowerCooldownAttachment.set(
        caster, PowerCooldownAttachment.get(caster).start(id, now, power.cooldownTicks()));
    for (final StatusEffectInstance effect : power.effects()) {
      caster.addStatusEffect(new StatusEffectInstance(effect), caster);
    }
    power.batForm().ifPresent(form -> BatForm.enter(caster));
    power.leap().ifPresent(leap -> leap.launch(caster, heading(caster)));
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
    return verdict;
  }
}
