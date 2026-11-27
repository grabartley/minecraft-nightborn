package com.grahambartley.nightborn.guild.party;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;

public record CampStay(
    int nights, long campDay, long pitchedAt, Optional<BlockPos> bell, long releasedAt) {

  public static final long UNKNOWN = -1L;
  public static final CampStay NONE = new CampStay(0, 0L, UNKNOWN);

  public static final MapCodec<CampStay> MAP_CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      Codec.INT.fieldOf("nights").forGetter(CampStay::nights),
                      Codec.LONG.fieldOf("camp_day").forGetter(CampStay::campDay),
                      Codec.LONG
                          .optionalFieldOf("pitched_at", UNKNOWN)
                          .forGetter(CampStay::pitchedAt),
                      BlockPos.CODEC.optionalFieldOf("guard_bell").forGetter(CampStay::bell),
                      Codec.LONG
                          .optionalFieldOf("released_at", UNKNOWN)
                          .forGetter(CampStay::releasedAt))
                  .apply(instance, CampStay::new));

  public static final Codec<CampStay> CODEC = MAP_CODEC.codec();

  public CampStay(final int nights, final long campDay, final long pitchedAt) {
    this(nights, campDay, pitchedAt, Optional.empty(), UNKNOWN);
  }

  public static CampStay guarding(final BlockPos bell, final long campDay, final long pitchedAt) {
    return new CampStay(0, campDay, pitchedAt, Optional.of(bell), UNKNOWN);
  }

  public boolean forGuard() {
    return bell.isPresent();
  }

  public boolean released() {
    return releasedAt != UNKNOWN;
  }

  public CampStay relieve(final long now) {
    return forGuard() && !released() ? new CampStay(nights, campDay, pitchedAt, bell, now) : this;
  }

  public boolean over(final long timeOfDay, final long now) {
    if (forGuard()) {
      return released()
          && (!PartySchedule.dusk(timeOfDay)
              || releasedAt > now
              || now - releasedAt >= PartySchedule.EVENING);
    }
    final long day = PartySchedule.day(timeOfDay);
    if (pitchedAt == UNKNOWN || pitchedAt > now || day < campDay) {
      return true;
    }
    return day - campDay >= nights || now - pitchedAt >= nights * PartySchedule.DAY;
  }
}
