package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.sight.Sights;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class GraveListener {

  static final int HEAR_EVERY_TICKS = 40;

  private GraveListener() {}

  static void register() {
    Sights.standing(GraveListener::show);
  }

  public static boolean listens(final boolean sneaking, final boolean vampire, final float range) {
    return sneaking && !vampire && range > 0.0F;
  }

  public static boolean hears(final boolean vampire, final boolean asleep) {
    return vampire && asleep;
  }

  private static void show(final ServerPlayerEntity hunter, final Map<Integer, Integer> shown) {
    final List<SeekGift> gifts = HeldSeekGifts.of(hunter);
    final float range = SeekGifts.listenRange(gifts);
    final int colour = SeekGifts.listenColour(gifts);
    if (!listens(hunter.isSneaking(), Vampires.isVampire(hunter), range)) {
      return;
    }
    final boolean heard = hunter.age % HEAR_EVERY_TICKS < 10;
    for (final LivingEntity sleeper :
        hunter
            .getServerWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                hunter.getBoundingBox().expand(range),
                candidate ->
                    candidate != hunter
                        && candidate.isAlive()
                        && candidate.squaredDistanceTo(hunter) <= (double) range * range
                        && hears(Vampires.isVampire(candidate), candidate.isSleeping()))) {
      shown.putIfAbsent(sleeper.getId(), colour);
      if (heard) {
        hunter.networkHandler.sendPacket(
            new PlaySoundS2CPacket(
                Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_WARDEN_HEARTBEAT),
                SoundCategory.HOSTILE,
                sleeper.getX(),
                sleeper.getY(),
                sleeper.getZ(),
                0.5F,
                0.6F,
                hunter.getRandom().nextLong()));
      }
    }
  }
}
