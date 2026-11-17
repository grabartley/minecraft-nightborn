package com.grahambartley.nightborn.sickness;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.feeding.BiteResistance;
import com.grahambartley.nightborn.feeding.BiteRules;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class BiteSickness {

  public static final TagKey<Item> CURES =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "cures_bite_sickness"));

  private BiteSickness() {}

  static final String BITTEN = "nightborn.sickness.bitten";
  static final String OLD_BLOOD = "nightborn.sickness.old_blood";

  public static boolean start(final ServerPlayerEntity human, final TurningSettings settings) {
    return start(human, BITTEN);
  }

  public static boolean startFromOldBlood(final ServerPlayerEntity human) {
    return start(human, OLD_BLOOD);
  }

  private static boolean start(final ServerPlayerEntity human, final String message) {
    if (Vampires.isVampire(human) || BiteSicknessAttachment.isSick(human)) {
      return false;
    }
    BiteSicknessAttachment.set(human, 0);
    human.sendMessage(Text.translatable(message), true);
    return true;
  }

  public static boolean startFromTurningBite(
      final LivingEntity victim,
      final boolean firstRoll,
      final Random random,
      final TurningSettings settings) {
    return startFromBite(
        victim,
        BiteRules.perThousand(settings.biteTurnChance()),
        settings.playersCanTurnPlayers(),
        firstRoll,
        random,
        settings);
  }

  public static boolean startFromBatBite(
      final LivingEntity victim, final Random random, final TurningSettings settings) {
    return startFromBite(victim, settings.batTurnChancePerThousand(), true, true, random, settings);
  }

  private static boolean startFromBite(
      final LivingEntity victim,
      final int chancePerThousand,
      final boolean allowed,
      final boolean firstRoll,
      final Random random,
      final TurningSettings settings) {
    return victim instanceof ServerPlayerEntity human
        && BiteRules.startsSickness(
            !Vampires.isVampire(human),
            true,
            allowed,
            firstRoll,
            random.nextInt(BiteRules.PER_THOUSAND),
            BiteResistance.sicknessChance(
                chancePerThousand, human.getAttributeValue(HunterAttributes.BITE_RESISTANCE)))
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
}
