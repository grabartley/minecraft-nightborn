package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.blood.BloodTicker;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.config.NightbornConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public final class MobBlood {

  static final String EXHAUSTION_KEY = "BloodExhaustion";
  static final String PENDING_KEY = "BloodPending";

  private float exhaustion;
  private float pending;

  public static BloodValue.Split gain(
      final float pending, final float taken, final int qualityPercent) {
    return BloodValue.split(
        pending + BloodValue.worth(taken, qualityPercent, BloodValue.FRESH_PERCENT));
  }

  public boolean hungry(final LivingEntity vampire, final NightbornConfig config) {
    return MobHunger.hungry(BloodAttachment.get(vampire), config.mobs().vampireHungerThreshold());
  }

  public void start(final LivingEntity vampire, final int roll, final NightbornConfig config) {
    BloodAttachment.set(
        vampire, MobHunger.startingBlood(config.mobs().vampireHungerThreshold(), roll));
  }

  public void tick(final LivingEntity vampire, final NightbornConfig config) {
    exhaustion =
        BloodTicker.metabolise(
            vampire, exhaustion, config.mobs().vampireBloodDrainSeconds(), config);
  }

  public void drink(
      final LivingEntity vampire,
      final LivingEntity victim,
      final float taken,
      final NightbornConfig config) {
    final BloodValue.Split split =
        gain(
            pending,
            taken,
            BloodQuality.of(victim.getType()).map(q -> q.worthPercent(config)).orElse(0));
    pending = split.remainder();
    BloodAttachment.set(vampire, BloodAttachment.get(vampire) + split.whole());
  }

  public void write(final NbtCompound nbt) {
    nbt.putFloat(EXHAUSTION_KEY, exhaustion);
    nbt.putFloat(PENDING_KEY, pending);
  }

  public void read(final NbtCompound nbt) {
    exhaustion = nbt.getFloat(EXHAUSTION_KEY);
    pending = nbt.getFloat(PENDING_KEY);
  }
}
