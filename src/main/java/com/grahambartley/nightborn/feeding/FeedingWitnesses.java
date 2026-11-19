package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.clothing.Noticing;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public final class FeedingWitnesses {

  private FeedingWitnesses() {}

  public static boolean anySees(
      final ServerWorld world,
      final LivingEntity vampire,
      @Nullable final LivingEntity victim,
      final int witnessRange) {
    final int range = Noticing.feeding(vampire, witnessRange);
    if (range <= 0) {
      return false;
    }
    return !world
        .getEntitiesByClass(
            LivingEntity.class,
            Box.of(vampire.getPos(), range * 2.0, range * 2.0, range * 2.0),
            other ->
                other != vampire
                    && watches(
                        other instanceof HunterEntity || other instanceof GuildMasterEntity,
                        other instanceof PlayerEntity player && !player.isSpectator(),
                        Vampires.isVampire(other))
                    && VillagerWitnesses.sees(
                        other == victim,
                        other.isAlive(),
                        other.isSleeping(),
                        other.squaredDistanceTo(vampire),
                        range,
                        () -> other.canSee(vampire)))
        .isEmpty();
  }

  static boolean watches(final boolean hunterMob, final boolean player, final boolean vampire) {
    return hunterMob || player && !vampire;
  }
}
