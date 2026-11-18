package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class StakedVampires {

  private StakedVampires() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(StakedVampires::afterDeath);
  }

  private static void afterDeath(final LivingEntity entity, final DamageSource source) {
    final NightbornConfig config = ServerConfigService.current();
    if (entity instanceof ServerPlayerEntity player
        && Vampires.isVampire(player)
        && Staking.staked(source)
        && !config.downed().hardcoreVampireDeath()) {
      penalise(player, config.skills().stakePenalty());
    }
  }

  private static void penalise(final ServerPlayerEntity player, final int percent) {
    final DynamicRegistryManager registries = player.getServerWorld().getRegistryManager();
    final TreeProgress before = SkillAttachment.get(player, SkillTree.VAMPIRE);
    final TreeProgress after =
        StakePenalty.apply(before, percent, skill -> Skills.lineOf(registries, skill));
    SkillAttachment.set(player, SkillTree.VAMPIRE, after);
    final int lost = before.earned() - after.earned();
    if (lost > 0) {
      player.sendMessage(Text.translatable("nightborn.staked.penalty", lost), false);
    }
  }
}
