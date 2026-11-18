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
    long lastMutterDay,
    long lastSeen) {

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
                          .forGetter(NightLog::lastMutterDay),
                      Codec.LONG.optionalFieldOf("last_seen", NEVER).forGetter(NightLog::lastSeen))
                  .apply(instance, NightLog::new));

  public static NightLog begun(
      final long comingNight, final long playedTicks, final int firstNights, final long dayLength) {
    final long first =
        playedTicks >= (long) firstNights * dayLength ? comingNight - firstNights : comingNight;
    return new NightLog(first, NEVER, false, false, Optional.empty(), NEVER, comingNight);
  }

  public NightLog seen(final long comingNight) {
    final long shift = lastSeen == NEVER || comingNight >= lastSeen ? 0 : comingNight - lastSeen;
    return new NightLog(
        firstNight + shift,
        shifted(lastNight, shift),
        watched,
        partyMet,
        greeter,
        shifted(lastMutterDay, shift),
        comingNight);
  }

  public boolean isNew(final long night, final int firstNights) {
    return night >= firstNight && night < firstNight + firstNights;
  }

  public boolean rolled(final long night) {
    return lastNight == night;
  }

  public NightLog rolledOn(final long night) {
    return new NightLog(firstNight, night, watched, partyMet, greeter, lastMutterDay, lastSeen);
  }

  public NightLog watchedOnce() {
    return new NightLog(firstNight, lastNight, true, partyMet, greeter, lastMutterDay, lastSeen);
  }

  public NightLog metParty(final UUID master) {
    return new NightLog(
        firstNight, lastNight, watched, true, Optional.of(master), lastMutterDay, lastSeen);
  }

  public NightLog greeted() {
    return new NightLog(
        firstNight, lastNight, watched, partyMet, Optional.empty(), lastMutterDay, lastSeen);
  }

  public boolean mutteredOn(final long day) {
    return lastMutterDay == day;
  }

  public NightLog mutteredTo(final long day) {
    return new NightLog(firstNight, lastNight, watched, partyMet, greeter, day, lastSeen);
  }

  private static long shifted(final long at, final long shift) {
    return at == NEVER ? NEVER : at + shift;
  }
}
