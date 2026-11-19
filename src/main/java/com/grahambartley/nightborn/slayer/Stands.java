package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.skill.Boost;
import com.grahambartley.nightborn.skill.Boosts;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.PowerGifts;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillPowerGifts;
import com.grahambartley.nightborn.skill.SkillRank;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class Stands {

  private Stands() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> {
          if (taken > 0.0F) {
            check(entity);
          }
        });
  }

  static void check(final LivingEntity entity) {
    if (entity.getWorld().isClient() || !entity.isAlive()) {
      return;
    }
    final Registry<Skill> registry =
        Skills.skills(entity.getWorld().getRegistryManager()).orElse(null);
    if (registry == null) {
      return;
    }
    final TreeProgress owned = SkillAttachment.owned(entity);
    final long now = entity.getWorld().getTime();
    for (final Identifier id : owned.ownedInOrder()) {
      final Skill skill = registry.get(id);
      if (skill == null) {
        continue;
      }
      for (final SkillRank rank : skill.held(owned.rank(id))) {
        for (final StandGift gift : rank.standGifts()) {
          final long left = PowerCooldownAttachment.get(entity).ticksLeft(id, now);
          if (gift.rises(entity.getHealth(), entity.getMaxHealth(), left)) {
            stand(entity, id, gift, now);
          }
        }
      }
    }
  }

  private static void stand(
      final LivingEntity entity, final Identifier id, final StandGift gift, final long now) {
    Boosts.start(entity, id, Boost.of(now, gift.durationTicks(), gift.attributes()));
    if (gift.steadfast()) {
      SlayerMarks.STEADFAST.set(entity, gift.durationTicks());
      FeedingSessions.release(entity);
    }
    final int cooldown =
        PowerCooldowns.shortened(
            gift.cooldownTicks(),
            PowerGifts.branchCooldown(
                PowerGifts.cooldownCut(SkillPowerGifts.forPower(entity, id))));
    PowerCooldownAttachment.set(
        entity, PowerCooldownAttachment.get(entity).start(id, now, cooldown));
    entity
        .getWorld()
        .playSound(
            null,
            entity.getBlockPos(),
            SoundEvents.BLOCK_BELL_RESONATE,
            SoundCategory.PLAYERS,
            1.0F,
            0.7F);
    if (entity instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.slayer.last_stand"), true);
    }
  }
}
