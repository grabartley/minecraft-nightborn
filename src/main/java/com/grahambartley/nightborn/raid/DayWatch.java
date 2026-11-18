package com.grahambartley.nightborn.raid;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public final class DayWatch {

  private DayWatch() {}

  public static boolean watchDay(final long day, final long house, final int every) {
    return every > 0 && Math.floorMod(day + Long.hashCode(house), every) == 0;
  }

  public static Optional<UUID> watcher(final Collection<UUID> living, final long day) {
    return living.stream().min(Comparator.comparingLong(member -> lot(member, day)));
  }

  static long lot(final UUID member, final long day) {
    long mixed = member.getMostSignificantBits() ^ member.getLeastSignificantBits() ^ day;
    mixed = (mixed ^ (mixed >>> 33)) * 0xff51afd7ed558ccdL;
    mixed = (mixed ^ (mixed >>> 33)) * 0xc4ceb9fe1a85ec53L;
    return mixed ^ (mixed >>> 33);
  }
}
