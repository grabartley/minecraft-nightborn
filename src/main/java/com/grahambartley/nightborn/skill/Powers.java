package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mind.Gazes;
import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.network.UsePowerPayload;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

  public static PowerUse.Verdict check(final LivingEntity caster, @Nullable final Identifier id) {
    return check(caster, id, power(caster, id));
  }

  public static PowerUse.Verdict check(
      final LivingEntity caster, @Nullable final Identifier id, final Optional<SkillPower> power) {
    return PowerUse.check(
        power.isPresent(),
        Downed.helpless(caster),
        Weakened.sources(caster).level(Downed.state(caster) != null) > 0,
        id == null
            ? 0L
            : PowerCooldownAttachment.get(caster).ticksLeft(id, caster.getWorld().getTime()),
        BloodAttachment.get(caster),
        power.map(SkillPower::blood).orElse(0),
        power
            .flatMap(SkillPower::mesmerise)
            .filter(mesmerise -> Mesmerise.target(caster, mesmerise.range()) == null)
            .isPresent());
  }

  public static PowerUse.Verdict cast(final LivingEntity caster, @Nullable final Identifier id) {
    final Optional<SkillPower> owned = power(caster, id);
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
    power
        .mesmerise()
        .ifPresent(
            mesmerise ->
                Gazes.start(caster, Mesmerise.target(caster, mesmerise.range()), mesmerise));
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
