package com.grahambartley.nightborn.guild.party;

public final class PartyMember {

  public static final double LEADER_NEAR = 64.0;

  private PartyMember() {}

  public static boolean staysInWorld(final boolean inParty, final boolean leaderNear) {
    return !inParty || leaderNear;
  }

  public static boolean wakes(final boolean sleeping, final long timeOfDay) {
    return sleeping && !PartySchedule.night(timeOfDay);
  }
}
