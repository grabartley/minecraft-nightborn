package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.sound.NightbornSounds;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvent;

public final class BaneHitSounds {

  static final float VOLUME = 0.9F;

  private BaneHitSounds() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> {
          if (!blocked && taken > 0.0F) {
            play(entity, source);
          }
        });
    ServerLivingEntityEvents.AFTER_DEATH.register(BaneHitSounds::play);
  }

  private static void play(final LivingEntity entity, final DamageSource source) {
    of(Vampires.isVampire(entity), Staking.staked(source), VampireBane.isSilverBlow(source))
        .ifPresent(
            sound ->
                entity
                    .getWorld()
                    .playSound(
                        null,
                        entity.getBlockPos(),
                        sound,
                        entity.getSoundCategory(),
                        VOLUME,
                        NightbornSounds.variedPitch(entity.getRandom())));
  }

  public static Optional<SoundEvent> of(
      final boolean vampire, final boolean stake, final boolean silver) {
    if (stake) {
      return Optional.of(NightbornSounds.STAKE_HIT);
    }
    return silver && vampire ? Optional.of(NightbornSounds.SILVER_HIT) : Optional.empty();
  }
}
