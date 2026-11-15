package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.core.VampireCore;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.faction.FactionPlayerList;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.sickness.BiteSicknessEffect;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public final class VampireTransitions {

  static final int TURNING_DARKNESS_TICKS = 80;

  private VampireTransitions() {}

  public static void turn(final ServerPlayerEntity player) {
    BiteSicknessAttachment.clear(player);
    player.removeStatusEffect(BiteSicknessEffect.ENTRY);
    if (Vampires.isVampire(player)) {
      return;
    }
    FactionAttachment.set(player, Faction.VAMPIRE);
    BloodAttachment.set(player, BloodAttachment.FULL);
    player.getHungerManager().setFoodLevel(BloodAttachment.FULL);
    player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
    player.addStatusEffect(
        new StatusEffectInstance(StatusEffects.DARKNESS, TURNING_DARKNESS_TICKS, 0, false, false));
    player
        .getWorld()
        .playSound(
            null,
            player.getBlockPos(),
            SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE,
            SoundCategory.PLAYERS,
            1.0F,
            0.5F);
    SkillAttributes.apply(player);
    player.sendMessage(Text.translatable("nightborn.turning.turned"), false);
    FactionPlayerList.refresh(player);
  }

  public static void makeHuman(final ServerPlayerEntity player) {
    BiteSicknessAttachment.clear(player);
    player.removeStatusEffect(BiteSicknessEffect.ENTRY);
    FeedingSessions.end(player);
    FactionAttachment.set(player, Faction.HUMAN);
    BloodAttachment.set(player, BloodAttachment.FULL);
    Downed.clear(player);
    Weakened.clear(player);
    VisibleStateAttachment.set(player, VisibleState.NONE);
    VampireCore.remove(player);
    SkillAttributes.apply(player);
    player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
    FactionPlayerList.refresh(player);
  }
}
