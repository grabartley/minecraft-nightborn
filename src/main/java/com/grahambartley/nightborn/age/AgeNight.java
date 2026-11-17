package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.core.NightTime;

public final class AgeNight {

  public static final long LENGTH = NightTime.DAWN - NightTime.DUSK;

  public record Step(NightWatch watch, int nightsLived) {}

  private AgeNight() {}

  public static long index(final long timeOfDay) {
    return Math.floorDiv(timeOfDay - NightTime.DUSK, NightTime.DAY_LENGTH);
  }

  public static long overlap(final long night, final long from, final long to) {
    final long start = night * NightTime.DAY_LENGTH + NightTime.DUSK;
    return Math.max(0L, Math.min(to, start + LENGTH) - Math.max(from, start));
  }

  public static Step step(final NightWatch watch, final long previous, final long now) {
    if (now <= previous) {
      return new Step(watch, 0);
    }
    final long from = Math.max(previous, now - NightTime.DAY_LENGTH);
    NightWatch stepped = watch;
    int lived = 0;
    for (long night = index(from); night <= index(now); night++) {
      final long ticks = overlap(night, from, now);
      if (ticks <= 0L) {
        continue;
      }
      final boolean same = stepped.night() == night;
      final int total = (int) Math.min(LENGTH, (same ? stepped.ticks() : 0) + ticks);
      final boolean alreadyCounted = same && stepped.counted();
      final boolean counts = !alreadyCounted && total * 2L > LENGTH;
      stepped = new NightWatch(night, total, alreadyCounted || counts);
      lived += counts ? 1 : 0;
    }
    return new Step(stepped, lived);
  }
}
