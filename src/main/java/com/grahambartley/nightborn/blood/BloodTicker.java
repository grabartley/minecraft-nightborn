package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeGifts;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public final class BloodTicker {

  static final float PINNED_SATURATION = 5.0F;
  static final int STARVING_SOURCE_TICKS = 2;

  private BloodTicker() {}

  public static void tick(
      final PlayerEntity vampire, final HungerManager hunger, final NightbornConfig config) {
    hunger.setExhaustion(
        metabolise(
            vampire,
            hunger.getExhaustion(),
            AgeGifts.drainSeconds(
                config.blood().restingDrainSeconds(),
                AgeAttachment.get(vampire).rank(),
                config.age().stageDrainSlower()),
            config));
    hunger.setFoodLevel(BloodAttachment.FULL);
    hunger.setSaturationLevel(PINNED_SATURATION);
  }

  public static float metabolise(
      final LivingEntity vampire,
      final float exhaustion,
      final int drainSeconds,
      final NightbornConfig config) {
    final DownedState downed = Downed.state(vampire);
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(
            new BloodMetabolism.State(
                BloodAttachment.get(vampire),
                exhaustion,
                vampire.getHealth(),
                vampire.getMaxHealth(),
                vampire.getWorld().getDifficulty(),
                vampire.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)
                    && (downed == null || !downed.isDowned()),
                BloodMetabolism.flightDrainSeconds(
                    BatForm.effort(vampire), Levitation.isLevitating(vampire), config.core()),
                vampire.age,
                VampireAttributes.value(vampire, VampireAttributes.HEALING_SPEED)),
            drainSeconds);
    BloodAttachment.set(vampire, outcome.blood());
    if (outcome.heals()) {
      vampire.heal(1.0F);
    }
    starve(vampire, outcome.blood(), config);
    return outcome.exhaustion();
  }

  private static void starve(
      final LivingEntity vampire, final int blood, final NightbornConfig config) {
    final Starvation.Outcome starving =
        Starvation.tick(
            StarvingAttachment.get(vampire),
            blood,
            vampire.getHealth(),
            vampire.getWorld().getDifficulty() == Difficulty.PEACEFUL,
            config.blood());
    StarvingAttachment.set(vampire, starving.ticks());
    Weakened.set(vampire, Starvation.SOURCE, starving.weakness(), STARVING_SOURCE_TICKS);
    if (starving.hurt() > 0.0F) {
      vampire.damage(vampire.getDamageSources().starve(), starving.hurt());
    }
  }
}
