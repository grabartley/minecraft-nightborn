package com.grahambartley.nightborn.wild;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record NightLog(
    long firstNight,
    long lastNight,
    boolean watched,
    boolean partyMet,
    Optional<UUID> greeter,
    long lastMutterDay) {

  public static final long NEVER = Long.MIN_VALUE;

  public static final Codec<NightLog> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.LONG.fieldOf("first_night").forGetter(NightLog::firstNight),
                      Codec.LONG
                          .optionalFieldOf("last_night", NEVER)
                          .forGetter(NightLog::lastNight),
                      Codec.BOOL.optionalFieldOf("watched", false).forGetter(NightLog::watched),
                      Codec.BOOL.optionalFieldOf("party_met", false).forGetter(NightLog::partyMet),
                      Uuids.CODEC.optionalFieldOf("greeter").forGetter(NightLog::greeter),
                      Codec.LONG
                          .optionalFieldOf("last_mutter_day", NEVER)
                          .forGetter(NightLog::lastMutterDay))
                  .apply(instance, NightLog::new));

  public static NightLog begun(
      final long comingNight, final long playedTicks, final int firstNights, final long dayLength) {
    final long first =
        playedTicks >= (long) firstNights * dayLength ? comingNight - firstNights : comingNight;
    return new NightLog(first, NEVER, false, false, Optional.empty(), NEVER);
  }

  public boolean isNew(final long night, final int firstNights) {
    return night >= firstNight && night < firstNight + firstNights;
  }

  public boolean rolled(final long night) {
    return lastNight == night;
  }

  public NightLog rolledOn(final long night) {
    return new NightLog(firstNight, night, watched, partyMet, greeter, lastMutterDay);
  }

  public NightLog watchedOnce() {
    return new NightLog(firstNight, lastNight, true, partyMet, greeter, lastMutterDay);
  }

  public NightLog metParty(final UUID master) {
    return new NightLog(firstNight, lastNight, watched, true, Optional.of(master), lastMutterDay);
  }

  public NightLog greeted() {
    return new NightLog(firstNight, lastNight, watched, partyMet, Optional.empty(), lastMutterDay);
  }

  public boolean mutteredOn(final long day) {
    return lastMutterDay == day;
  }

  public NightLog mutteredTo(final long day) {
    return new NightLog(firstNight, lastNight, watched, partyMet, greeter, day);
  }
}
