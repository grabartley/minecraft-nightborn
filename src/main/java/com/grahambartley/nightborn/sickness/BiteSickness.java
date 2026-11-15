package com.grahambartley.nightborn.sickness;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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

public final class BiteSickness {

  public static final TagKey<Item> CURES =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "cures_bite_sickness"));

  static final int EFFECT_REFRESH_TICKS = 20;
  static final int SYMPTOM_TICKS = 60;
  static final int DISPLAY_DRIFT_TICKS = 40;

  private BiteSickness() {}

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(BiteSickness::tick);
  }

  public static boolean start(final ServerPlayerEntity human, final TurningSettings settings) {
    if (Vampires.isVampire(human) || BiteSicknessAttachment.isSick(human)) {
      return false;
    }
    BiteSicknessAttachment.set(human, SicknessStage.totalTicks(settings.sicknessDays()));
    human.sendMessage(Text.translatable("nightborn.sickness.bitten"), true);
    return true;
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
    return true;
  }

  public static void onEaten(final ServerPlayerEntity player, final ItemStack food) {
    if (food.isIn(CURES) && player.hasStatusEffect(StatusEffects.WEAKNESS)) {
      cure(player);
    }
  }

  private static void tick(final MinecraftServer server) {
    final TurningSettings settings = ServerConfigService.current().turning();
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      if (BiteSicknessAttachment.isSick(player)) {
        tick(player, settings);
      }
    }
  }

  static void tick(final ServerPlayerEntity player, final TurningSettings settings) {
    final int left = BiteSicknessAttachment.ticksLeft(player) - 1;
    if (left <= 0) {
      VampireTransitions.turn(player);
      return;
    }
    final SicknessStage before = SicknessStage.of(left + 1, settings.sicknessDays());
    BiteSicknessAttachment.set(player, left);
    final SicknessStage stage = SicknessStage.of(left, settings.sicknessDays());
    if (stage.daysLeft() != before.daysLeft() || player.age % 6000 == 0) {
      player.sendMessage(Text.translatable("nightborn.sickness.days_left", stage.daysLeft()), true);
    }
    if (player.age % EFFECT_REFRESH_TICKS == 0) {
      showSymptoms(player, stage, left);
    }
  }

  private static void showSymptoms(
      final ServerPlayerEntity player, final SicknessStage stage, final int ticksLeft) {
    final StatusEffectInstance shown = player.getStatusEffect(BiteSicknessEffect.ENTRY);
    if (shown == null
        || shown.getAmplifier() != stage.day() - 1
        || Math.abs(shown.getDuration() - ticksLeft) > DISPLAY_DRIFT_TICKS) {
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
