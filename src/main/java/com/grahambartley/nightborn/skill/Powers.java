package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.network.UsePowerPayload;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
    final TreeProgress progress = SkillAttachment.get(player, SkillTree.of(player));
    final List<Identifier> bar = PowerBar.slots(skills, progress);
    final Identifier id = slot >= 0 && slot < bar.size() ? bar.get(slot) : null;
    final SkillPower power = id == null ? null : skills.get(id).power().orElseThrow();
    final long now = player.getServerWorld().getTime();
    final PowerCooldowns cooldowns = PowerCooldownAttachment.get(player);
    final PowerUse.Verdict verdict =
        PowerUse.check(
            power != null,
            Downed.helpless(player),
            Weakened.sources(player).level(Downed.state(player) != null) > 0,
            id == null ? 0L : cooldowns.ticksLeft(id, now),
            BloodAttachment.get(player),
            power == null ? 0 : power.blood());
    if (verdict != PowerUse.Verdict.READY) {
      if (verdict != PowerUse.Verdict.NO_POWER) {
        player.sendMessage(Text.translatable(verdict.translationKey()), true);
      }
      return verdict;
    }
    if (power.blood() > 0) {
      BloodAttachment.set(player, BloodAttachment.get(player) - power.blood());
    }
    PowerCooldownAttachment.set(player, cooldowns.start(id, now, power.cooldownTicks()));
    for (final StatusEffectInstance effect : power.effects()) {
      player.addStatusEffect(new StatusEffectInstance(effect), player);
    }
    power
        .sound()
        .ifPresent(
            sound ->
                player
                    .getServerWorld()
                    .playSound(
                        null,
                        player.getBlockPos(),
                        SoundEvent.of(sound),
                        SoundCategory.PLAYERS,
                        1.0F,
                        1.0F));
    return verdict;
  }
}
