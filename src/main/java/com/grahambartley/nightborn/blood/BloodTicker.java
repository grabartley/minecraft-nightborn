package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;

public final class BloodTicker {

  static final float PINNED_SATURATION = 5.0F;

  private BloodTicker() {}

  public static void tick(
      final PlayerEntity vampire, final HungerManager hunger, final BloodSettings settings) {
    final DownedState downed = Downed.state(vampire);
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(
            new BloodMetabolism.State(
                BloodAttachment.get(vampire),
                hunger.getExhaustion(),
                vampire.getHealth(),
                vampire.getMaxHealth(),
                vampire.getWorld().getDifficulty(),
                vampire.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)
                    && (downed == null || !downed.isDowned()),
                vampire.age),
            settings.restingDrainSeconds());
    hunger.setExhaustion(outcome.exhaustion());
    hunger.setFoodLevel(BloodAttachment.FULL);
    hunger.setSaturationLevel(PINNED_SATURATION);
    BloodAttachment.set(vampire, outcome.blood());
    if (outcome.heals()) {
      vampire.heal(1.0F);
    }
    if (outcome.starves()) {
      vampire.damage(vampire.getDamageSources().starve(), 1.0F);
    }
  }
}
