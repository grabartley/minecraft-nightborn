package com.grahambartley.nightborn.guild.party;

import java.util.Optional;
import java.util.UUID;

public final class PartyMember {

  public static final double LEADER_NEAR = 64.0;

  private PartyMember() {}

  public static boolean staysInWorld(final boolean inParty, final boolean leaderNear) {
    return !inParty || leaderNear;
  }

  public static boolean follows(final Optional<UUID> leader, final UUID master) {
    return leader.filter(master::equals).isPresent();
  }

  public static boolean fellows(
      final UUID one,
      final Optional<UUID> oneQuarry,
      final Optional<UUID> oneLeader,
      final UUID other,
      final Optional<UUID> otherQuarry,
      final Optional<UUID> otherLeader) {
    return oneQuarry.isPresent() && oneQuarry.equals(otherQuarry)
        || oneLeader.isPresent() && oneLeader.equals(otherLeader)
        || follows(otherLeader, one)
        || follows(oneLeader, other);
  }

  public static boolean wakes(final boolean sleeping, final long timeOfDay) {
    return sleeping && !PartySchedule.night(timeOfDay);
  }
}
