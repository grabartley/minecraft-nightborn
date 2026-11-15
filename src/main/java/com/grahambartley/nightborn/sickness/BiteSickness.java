package com.grahambartley.nightborn.sickness;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.feeding.BiteRules;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class BiteSickness {

  public static final TagKey<Item> CURES =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "cures_bite_sickness"));

  static final int EFFECT_REFRESH_TICKS = 20;
  static final int SYMPTOM_TICKS = 60;

  private static final SicknessClock CLOCK = new SicknessClock();

  private BiteSickness() {}

  public static void register() {
    ServerLifecycleEvents.SERVER_STARTED.register(server -> CLOCK.reset());
    ServerTickEvents.END_SERVER_TICK.register(BiteSickness::tick);
  }

  public static boolean start(final ServerPlayerEntity human, final TurningSettings settings) {
    if (Vampires.isVampire(human) || BiteSicknessAttachment.isSick(human)) {
      return false;
    }
    BiteSicknessAttachment.set(human, 0);
    human.sendMessage(Text.translatable("nightborn.sickness.bitten"), true);
    return true;
  }

  public static boolean startFromBite(
      final LivingEntity victim,
      final int chancePercent,
      final boolean playersCanTurnPlayers,
      final boolean firstRoll,
      final Random random,
      final TurningSettings settings) {
    return victim instanceof ServerPlayerEntity human
        && BiteRules.startsSickness(
            !Vampires.isVampire(human),
            playersCanTurnPlayers,
            firstRoll,
            random.nextInt(100),
            BiteRules.resistedChance(
                chancePercent, human.getAttributeValue(HunterAttributes.BITE_RESISTANCE)))
        && start(human, settings);
  }

  public static boolean cure(final ServerPlayerEntity player) {
    if (!BiteSicknessAttachment.isSick(player)) {
      return false;
    }
    BiteSicknessAttachment.clear(player);
    player.removeStatusEffect(BiteSicknessEffect.ENTRY);
    player
        .getWorld()
        .playSound(
            null,
            player.getBlockPos(),
            SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
            SoundCategory.PLAYERS,
            1.0F,
            1.0F);
    player.sendMessage(Text.translatable("nightborn.sickness.cured"), true);
    MilestoneCriterion.reach(player, Milestone.SICKNESS_CURED);
    return true;
  }

  public static void onEaten(final ServerPlayerEntity player, final ItemStack food) {
    if (SicknessRules.cures(
        food.isIn(CURES),
        player.hasStatusEffect(StatusEffects.WEAKNESS),
        BiteSicknessAttachment.isSick(player))) {
      cure(player);
    }
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
    final int run = SicknessClock.run(runBefore, step);
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
        stage.day() - 1,
        ticksLeft)) {
      player.addStatusEffect(
          new StatusEffectInstance(
              BiteSicknessEffect.ENTRY, ticksLeft, stage.day() - 1, false, false, true));
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
