package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodMetabolism;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.blood.BloodTicker;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.levitation.Levitation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.GameRules;

public final class MobBlood {

  private float exhaustion;
  private float pending;

  public int blood(final LivingEntity vampire) {
    return BloodAttachment.get(vampire);
  }

  public boolean hungry(final LivingEntity vampire, final NightbornConfig config) {
    return MobHunger.hungry(blood(vampire), config.mobs().vampireHungerThreshold());
  }

  public void start(final LivingEntity vampire, final int roll, final NightbornConfig config) {
    BloodAttachment.set(
        vampire, MobHunger.startingBlood(config.mobs().vampireHungerThreshold(), roll));
  }

  public void tick(final LivingEntity vampire, final NightbornConfig config) {
    final DownedState downed = Downed.state(vampire);
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(
            new BloodMetabolism.State(
                blood(vampire),
                exhaustion,
                vampire.getHealth(),
                vampire.getMaxHealth(),
                vampire.getWorld().getDifficulty(),
                vampire.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)
                    && (downed == null || !downed.isDowned()),
                Levitation.isLevitating(vampire),
                vampire.age),
            config.mobs().vampireBloodDrainSeconds(),
            config.core().levitationDrainSeconds());
    exhaustion = outcome.exhaustion();
    BloodAttachment.set(vampire, outcome.blood());
    if (outcome.heals()) {
      vampire.heal(1.0F);
    }
    BloodTicker.starve(vampire, outcome.blood(), config);
  }

  public void drink(
      final LivingEntity vampire,
      final LivingEntity victim,
      final float taken,
      final NightbornConfig config) {
    final int quality =
        BloodQuality.of(victim.getType()).map(q -> q.worthPercent(config)).orElse(0);
    final BloodValue.Split split =
        BloodValue.split(pending + BloodValue.worth(taken, quality, BloodValue.FRESH_PERCENT));
    pending = split.remainder();
    BloodAttachment.set(vampire, blood(vampire) + split.whole());
  }
}
