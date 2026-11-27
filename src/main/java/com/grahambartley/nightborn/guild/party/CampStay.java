package com.grahambartley.nightborn.guild.party;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CampStay(int nights, long campDay, long pitchedAt) {

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
                          .forGetter(CampStay::pitchedAt))
                  .apply(instance, CampStay::new));

  public static final Codec<CampStay> CODEC = MAP_CODEC.codec();

  public boolean over(final long timeOfDay, final long now) {
    final long day = PartySchedule.day(timeOfDay);
    if (pitchedAt == UNKNOWN || pitchedAt > now || day < campDay) {
      return true;
    }
    return day - campDay >= nights || now - pitchedAt >= nights * PartySchedule.DAY;
  }
}
