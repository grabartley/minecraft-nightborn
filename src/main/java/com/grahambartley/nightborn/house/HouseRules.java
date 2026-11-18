package com.grahambartley.nightborn.house;

import java.util.Locale;
import java.util.Optional;

public final class HouseRules {

  public static final int ADULT = 1;

  public enum Invite {
    WELCOME,
    NOT_LORD,
    NOT_VAMPIRE,
    NOT_YOUNGER,
    FULL,
    ALREADY_MEMBER,
    HEAD,
    LEADS_HOUSE,
    FAR_AWAY;

    public String key() {
      return "nightborn.house.invite." + name().toLowerCase(Locale.ROOT);
    }
  }

  public enum Founding {
    NONE,
    FOUND,
    CLAIM,
    LIVED_IN,
    CLAIMED;

    public Optional<String> key() {
      return this == NONE
          ? Optional.empty()
          : Optional.of("nightborn.house.founding." + name().toLowerCase(Locale.ROOT));
    }
  }

  public record Guest(
      boolean vampire,
      int stage,
      boolean alreadyHere,
      boolean head,
      boolean leadsHouse,
      boolean sameWorld) {}

  private HouseRules() {}

  public static boolean opensWheel(
      final boolean mainHand,
      final boolean emptyHand,
      final boolean sneaking,
      final boolean spectator,
      final boolean houseSubject) {
    return mainHand && emptyHand && !sneaking && !spectator && houseSubject;
  }

  public static boolean intruder(
      final boolean player, final boolean vampire, final boolean exempt, final boolean hunter) {
    return hunter || (player && !vampire && !exempt);
  }

  public static boolean mayFound(final int ageRank) {
    return ageRank >= ADULT;
  }

  public static Invite invite(
      final boolean lord,
      final int lordStage,
      final int size,
      final int capacity,
      final Guest guest) {
    if (!lord) {
      return Invite.NOT_LORD;
    }
    if (!guest.vampire()) {
      return Invite.NOT_VAMPIRE;
    }
    if (guest.alreadyHere()) {
      return Invite.ALREADY_MEMBER;
    }
    if (guest.head()) {
      return Invite.HEAD;
    }
    if (guest.leadsHouse()) {
      return Invite.LEADS_HOUSE;
    }
    if (guest.stage() >= lordStage) {
      return Invite.NOT_YOUNGER;
    }
    if (!guest.sameWorld()) {
      return Invite.FAR_AWAY;
    }
    return size >= capacity ? Invite.FULL : Invite.WELCOME;
  }

  public static Founding founding(
      final int ageRank,
      final boolean leadsHouse,
      final boolean memberElsewhere,
      final boolean ownCoffin,
      final Optional<Claim> claim) {
    if (!mayFound(ageRank) || memberElsewhere) {
      return Founding.NONE;
    }
    if (claim.isPresent()) {
      return claim.get().decide();
    }
    return ownCoffin && !leadsHouse ? Founding.FOUND : Founding.NONE;
  }

  public record Claim(boolean householdGone, boolean claimedByOther, boolean heldAlready) {

    Founding decide() {
      if (heldAlready) {
        return Founding.NONE;
      }
      if (claimedByOther) {
        return Founding.CLAIMED;
      }
      return householdGone ? Founding.CLAIM : Founding.LIVED_IN;
    }
  }
}
