package com.grahambartley.nightborn.hunter;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.mob.stage.Staged;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
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

  public static Optional<UUID> hunter(
      final boolean vampire,
      @Nullable final DownedState state,
      final boolean sunKill,
      final Optional<UUID> staker) {
    if (!vampire) {
      return Optional.empty();
    }
    if (staker.isPresent()) {
      return staker;
    }
    return state != null && sunKill ? state.downedBy() : Optional.empty();
  }

  private static void afterDeath(final LivingEntity vampire, final DamageSource source) {
    final MinecraftServer server = vampire.getServer();
    if (server == null) {
      return;
    }
    final Optional<UUID> staker = Staking.staked(source) ? Downed.hunter(source) : Optional.empty();
    final String message = staker.isPresent() ? "nightborn.hunt.staked" : "nightborn.hunt.earned";
    hunter(
            Vampires.isVampire(vampire),
            Downed.state(vampire),
            source.isOf(NightbornDamage.SUN),
            staker)
        .map(id -> server.getPlayerManager().getPlayer(id))
        .ifPresent(
            hunter -> {
              if (staker.isPresent()) {
                MilestoneCriterion.reach(hunter, Milestone.VAMPIRE_STAKED);
              }
              reward(hunter, vampire, message);
            });
  }

  private static void reward(
      final ServerPlayerEntity hunter, final LivingEntity vampire, final String message) {
    if (!SkillEarning.fromHunt(
        hunter,
        ServerConfigService.current().skills(),
        vampire instanceof Staged staged ? staged.stageRank() : 0)) {
      return;
    }
    hunter.sendMessage(Text.translatable(message, vampire.getDisplayName()), true);
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
