package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class EldestRewards {

  static final String MESSAGE = "nightborn.ritual.eldest_ended";

  private EldestRewards() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(EldestRewards::afterDeath);
  }

  public static boolean pays(final boolean staked, final boolean sunKill) {
    return staked || sunKill;
  }

  private static void afterDeath(final LivingEntity dead, final DamageSource source) {
    final MinecraftServer server = dead.getServer();
    if (server == null
        || !(dead instanceof EldestEntity eldest)
        || !pays(Staking.staked(source), source.isOf(NightbornDamage.SUN))) {
      return;
    }
    for (final var id : eldest.fighters()) {
      final ServerPlayerEntity hunter = server.getPlayerManager().getPlayer(id);
      if (hunter != null) {
        reward(hunter, eldest);
      }
    }
  }

  private static void reward(final ServerPlayerEntity hunter, final EldestEntity eldest) {
    if (!SkillEarning.fromEldest(hunter, ServerConfigService.current().ritual())) {
      return;
    }
    hunter.sendMessage(Text.translatable(MESSAGE, eldest.getDisplayName()), false);
    hunter
        .getServerWorld()
        .playSound(
            null,
            hunter.getBlockPos(),
            SoundEvents.BLOCK_BELL_RESONATE,
            SoundCategory.PLAYERS,
            1.0F,
            0.6F);
  }
}
