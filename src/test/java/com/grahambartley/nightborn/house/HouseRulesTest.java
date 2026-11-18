package com.grahambartley.nightborn.house;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseRulesTest {

  @ParameterizedTest(name = "lord {0}, vampire {1}, stages {2}/{3}, {4} of {5}: {11}")
  @CsvSource({
    "true, true, 9, 6, 1, 2, false, false, false, true, WELCOME",
    "false, true, 9, 6, 1, 2, false, false, false, true, NOT_LORD",
    "true, false, 9, 0, 1, 2, false, false, false, true, NOT_VAMPIRE",
    "true, true, 9, 6, 1, 2, true, false, false, true, ALREADY_MEMBER",
    "true, true, 9, 6, 1, 2, false, true, false, true, HEAD",
    "true, true, 9, 6, 1, 2, false, false, true, true, LEADS_HOUSE",
    "true, true, 6, 6, 1, 2, false, false, false, true, NOT_YOUNGER",
    "true, true, 6, 9, 1, 2, false, false, false, true, NOT_YOUNGER",
    "true, true, 9, 6, 1, 2, false, false, false, false, FAR_AWAY",
    "true, true, 9, 6, 2, 2, false, false, false, true, FULL",
    "true, true, 9, 6, 0, 0, false, false, false, true, FULL"
  })
  @DisplayName("a lord invites only a younger vampire from elsewhere while the house has room")
  void invite(
      final boolean lord,
      final boolean vampire,
      final int lordStage,
      final int guestStage,
      final int size,
      final int capacity,
      final boolean alreadyHere,
      final boolean head,
      final boolean leadsHouse,
      final boolean sameWorld,
      final HouseRules.Invite expected) {
    assertEquals(
        expected,
        HouseRules.invite(
            lord,
            lordStage,
            size,
            capacity,
            new HouseRules.Guest(vampire, guestStage, alreadyHere, head, leadsHouse, sameWorld)));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "NOT_LORD, nightborn.house.invite.not_lord",
    "NOT_YOUNGER, nightborn.house.invite.not_younger",
    "FAR_AWAY, nightborn.house.invite.far_away"
  })
  @DisplayName("each refusal names itself to the lord")
  void inviteKeys(final HouseRules.Invite verdict, final String key) {
    assertEquals(key, verdict.key());
  }

  @ParameterizedTest(name = "age {0}, leads {1}, member {2}, own coffin {3}: {4}")
  @CsvSource({
    "1, false, false, true, FOUND",
    "3, false, false, true, FOUND",
    "0, false, false, true, NONE",
    "1, true, false, true, NONE",
    "1, false, true, true, NONE",
    "1, false, false, false, NONE"
  })
  @DisplayName("an adult with no house founds one by lying in their own coffin")
  void foundsAtCoffin(
      final int age,
      final boolean leads,
      final boolean member,
      final boolean own,
      final HouseRules.Founding expected) {
    assertEquals(expected, HouseRules.founding(age, leads, member, own, Optional.empty()));
  }

  @ParameterizedTest(name = "age {0}, gone {1}, claimed {2}, held {3}, member {4}: {5}")
  @CsvSource({
    "1, true, false, false, false, CLAIM",
    "1, false, false, false, false, LIVED_IN",
    "1, true, true, false, false, CLAIMED",
    "1, true, false, true, false, NONE",
    "0, true, false, false, false, NONE",
    "2, true, false, false, true, NONE"
  })
  @DisplayName("an adult claims a structure by its old head's coffin once its household is gone")
  void claims(
      final int age,
      final boolean gone,
      final boolean claimed,
      final boolean held,
      final boolean member,
      final HouseRules.Founding expected) {
    assertEquals(
        expected,
        HouseRules.founding(
            age, true, member, false, Optional.of(new HouseRules.Claim(gone, claimed, held))));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "NONE, ''",
    "FOUND, nightborn.house.founding.found",
    "CLAIM, nightborn.house.founding.claim",
    "LIVED_IN, nightborn.house.founding.lived_in",
    "CLAIMED, nightborn.house.founding.claimed"
  })
  @DisplayName("every founding but nothing at all tells the vampire what happened")
  void foundingKeys(final HouseRules.Founding verdict, final String key) {
    assertEquals(key.isEmpty() ? Optional.empty() : Optional.of(key), verdict.key());
  }

  @ParameterizedTest(name = "player {0}, vampire {1}, exempt {2}, hunter {3} -> {4}")
  @CsvSource({
    "true, false, false, false, true",
    "true, true, false, false, false",
    "true, false, true, false, false",
    "false, false, false, true, true",
    "false, false, false, false, false"
  })
  @DisplayName("a guard turns on human players and hunters, never on vampires or onlookers")
  void intruder(
      final boolean player,
      final boolean vampire,
      final boolean exempt,
      final boolean hunter,
      final boolean expected) {
    assertEquals(expected, HouseRules.intruder(player, vampire, exempt, hunter));
  }

  @ParameterizedTest(name = "main {0}, empty {1}, sneaking {2}, spectator {3}, subject {4} -> {5}")
  @CsvSource({
    "true, true, false, false, true, true",
    "false, true, false, false, true, false",
    "true, false, false, false, true, false",
    "true, true, true, false, true, false",
    "true, true, false, true, true, false",
    "true, true, false, false, false, false"
  })
  @DisplayName("an empty hand opens the wheel on a vampire or a player, unless sneaking to bite")
  void opensWheel(
      final boolean main,
      final boolean empty,
      final boolean sneaking,
      final boolean spectator,
      final boolean subject,
      final boolean expected) {
    assertEquals(expected, HouseRules.opensWheel(main, empty, sneaking, spectator, subject));
  }
}
