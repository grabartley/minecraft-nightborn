package com.grahambartley.nightborn.hunter;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class HuntRewards {

  private HuntRewards() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(HuntRewards::afterDeath);
  }

  public static Optional<UUID> hunter(@Nullable final DownedState state, final boolean sunKill) {
    return state != null && sunKill ? state.downedBy() : Optional.empty();
  }

  private static void afterDeath(final LivingEntity vampire, final DamageSource source) {
    final MinecraftServer server = vampire.getServer();
    if (server == null) {
      return;
    }
    hunter(Downed.state(vampire), source.isOf(NightbornDamage.SUN))
        .map(id -> server.getPlayerManager().getPlayer(id))
        .ifPresent(hunter -> reward(hunter, vampire));
  }

  private static void reward(final ServerPlayerEntity hunter, final LivingEntity vampire) {
    if (!SkillEarning.fromHunt(hunter, ServerConfigService.current().skills())) {
      return;
    }
    hunter.sendMessage(Text.translatable("nightborn.hunt.earned", vampire.getDisplayName()), true);
    hunter
        .getServerWorld()
        .playSound(
            null,
            hunter.getBlockPos(),
            SoundEvents.BLOCK_BELL_RESONATE,
            SoundCategory.PLAYERS,
            0.6F,
            0.8F);
  }
}
