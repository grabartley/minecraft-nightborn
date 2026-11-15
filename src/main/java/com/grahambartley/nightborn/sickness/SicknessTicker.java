package com.grahambartley.nightborn.sickness;

import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class SicknessTicker {

  static final int EFFECT_REFRESH_TICKS = 20;
  static final int SYMPTOM_TICKS = 60;

  private static final SicknessClock CLOCK = new SicknessClock();

  private SicknessTicker() {}

  public static void register() {
    ServerLifecycleEvents.SERVER_STARTED.register(server -> CLOCK.reset());
    ServerTickEvents.END_SERVER_TICK.register(SicknessTicker::tick);
  }

  private static void tick(final MinecraftServer server) {
    final int step = CLOCK.advance(server.getOverworld().getTimeOfDay());
    final TurningSettings settings = ServerConfigService.current().turning();
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      if (BiteSicknessAttachment.isSick(player)) {
        tick(player, settings, step);
      }
    }
  }

  public static void tick(
      final ServerPlayerEntity player, final TurningSettings settings, final int step) {
    final int runBefore = BiteSicknessAttachment.ticksRun(player);
    final int run = SicknessStage.advance(runBefore, step);
    final int left = SicknessStage.ticksLeft(run, settings.sicknessDays());
    if (left <= 0) {
      VampireTransitions.turn(player);
      return;
    }
    final SicknessStage before = SicknessStage.of(runBefore, settings.sicknessDays());
    BiteSicknessAttachment.set(player, run);
    final SicknessStage stage = SicknessStage.of(run, settings.sicknessDays());
    if (SicknessRules.remindsOfDaysLeft(before.daysLeft(), stage.daysLeft(), player.age)) {
      player.sendMessage(Text.translatable("nightborn.sickness.days_left", stage.daysLeft()), true);
    }
    if (player.age % EFFECT_REFRESH_TICKS == 0) {
      showSymptoms(player, stage, left);
    }
  }

  private static void showSymptoms(
      final ServerPlayerEntity player, final SicknessStage stage, final int ticksLeft) {
    final StatusEffectInstance shown = player.getStatusEffect(BiteSicknessEffect.ENTRY);
    if (SicknessRules.refreshesDisplay(
        shown != null,
        shown == null ? 0 : shown.getAmplifier(),
        shown == null ? 0 : shown.getDuration(),
        stage.stage() - 1,
        ticksLeft)) {
      player.removeStatusEffect(BiteSicknessEffect.ENTRY);
      player.addStatusEffect(
          new StatusEffectInstance(
              BiteSicknessEffect.ENTRY, ticksLeft, stage.stage() - 1, false, false, true));
    }
    if (stage.hunger()) {
      symptom(
          player,
          new StatusEffectInstance(StatusEffects.HUNGER, SYMPTOM_TICKS, 0, true, false, true));
    }
    if (stage.weakness()) {
      symptom(
          player,
          new StatusEffectInstance(StatusEffects.WEAKNESS, SYMPTOM_TICKS, 0, true, false, true));
    }
    if (stage.daylightSlowness() && player.getWorld().isDay()) {
      symptom(
          player,
          new StatusEffectInstance(StatusEffects.SLOWNESS, SYMPTOM_TICKS, 0, true, false, true));
    }
  }

  private static void symptom(final ServerPlayerEntity player, final StatusEffectInstance effect) {
    final StatusEffectInstance current = player.getStatusEffect(effect.getEffectType());
    if (current == null || current.getDuration() < SYMPTOM_TICKS - EFFECT_REFRESH_TICKS) {
      player.addStatusEffect(effect);
    }
  }
}
