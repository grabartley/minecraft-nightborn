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

  public static boolean wakes(final boolean sleeping, final long timeOfDay) {
    return sleeping && !PartySchedule.night(timeOfDay);
  }
}
