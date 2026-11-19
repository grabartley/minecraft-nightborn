package com.grahambartley.nightborn.guild.veteran;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VeteranChoiceTest {

  static final VeteranAttack.Trigger LOCKED = new VeteranAttack.Trigger(3.0, 2, 4.0, 20, 0.5);

  static VeteranAttack attack(
      final VeteranAttack.Kind kind, final double reachMin, final double reach) {
    return new VeteranAttack(
        kind,
        "clip",
        Identifier.ofVanilla("sound"),
        Optional.empty(),
        20,
        100,
        reachMin,
        reach,
        3,
        1.0,
        60,
        6.0F,
        Optional.empty(),
        Optional.empty(),
        false,
        LOCKED);
  }

  private static VeteranChoice.Situation at(
      final double distance,
      final boolean airborne,
      final boolean bat,
      final int near,
      final int still) {
    return new VeteranChoice.Situation(true, distance, airborne, bat, near, still, false, 1.0);
  }

  @ParameterizedTest(name = "{0} away, airborne {1}, bat {2}: chain {3}")
  @CsvSource({
    "2.0, false, false, false",
    "3.0, false, false, false",
    "2.0, true, false, true",
    "2.0, false, true, true",
    "5.0, false, false, true",
    "12.0, false, false, true",
    "12.5, false, false, false"
  })
  void throwsTheChainAtAVampireOutOfReachOrInTheAir(
      final double distance, final boolean airborne, final boolean bat, final boolean fits) {
    assertEquals(
        fits,
        VeteranChoice.fits(
            attack(VeteranAttack.Kind.SILVER_CHAIN, 0.0, 12.0), at(distance, airborne, bat, 1, 0)));
  }

  @ParameterizedTest(name = "{0} away: volley {1}")
  @CsvSource({"7.9, false", "8.0, true", "20.0, true", "20.1, false"})
  void loosesTheVolleyAtMiddleRange(final double distance, final boolean fits) {
    assertEquals(
        fits,
        VeteranChoice.fits(
            attack(VeteranAttack.Kind.BOLT_VOLLEY, 8.0, 20.0), at(distance, false, false, 1, 0)));
  }

  @ParameterizedTest(name = "{0} vampires near, still {1} ticks: arc {2}")
  @CsvSource({"1, 0, false", "2, 0, true", "1, 19, false", "1, 20, true", "3, 40, true"})
  void throwsTheArcAtACrowdOrAVampireStandingStill(
      final int near, final int still, final boolean fits) {
    assertEquals(
        fits,
        VeteranChoice.fits(
            attack(VeteranAttack.Kind.HOLY_WATER_ARC, 0.0, 10.0),
            at(6.0, false, false, near, still)));
  }

  @ParameterizedTest(name = "mesmerised ally {0}, party health {1}: rally {2}")
  @CsvSource({"false, 1.0, false", "true, 1.0, true", "false, 0.5, true", "false, 0.51, false"})
  void ralliesWhenAnAllyIsMesmerisedOrThePartyIsHalfSpent(
      final boolean mesmerised, final double health, final boolean fits) {
    assertEquals(
        fits,
        VeteranChoice.fits(
            attack(VeteranAttack.Kind.RALLYING_CRY, 0.0, 0.0),
            new VeteranChoice.Situation(false, 40.0, false, false, 0, 0, mesmerised, health)));
  }

  @ParameterizedTest(name = "sees {0}: chain {1}")
  @CsvSource({"true, true", "false, false"})
  void needsSightForAnythingThrown(final boolean sees, final boolean fits) {
    assertEquals(
        fits,
        VeteranChoice.fits(
            attack(VeteranAttack.Kind.SILVER_CHAIN, 0.0, 12.0),
            new VeteranChoice.Situation(sees, 6.0, false, false, 1, 0, false, 1.0)));
  }

  @ParameterizedTest(name = "now {0}, chain ready at {1}: picks {2}")
  @CsvSource({"100, 0, chain", "100, 101, volley", "101, 101, chain"})
  void picksTheFirstReadyAttackThatFits(final long now, final long chainReady, final String pick) {
    final Map<Identifier, VeteranAttack> attacks = new LinkedHashMap<>();
    attacks.put(
        Identifier.of("nightborn", "chain"), attack(VeteranAttack.Kind.SILVER_CHAIN, 0.0, 20.0));
    attacks.put(
        Identifier.of("nightborn", "volley"), attack(VeteranAttack.Kind.BOLT_VOLLEY, 8.0, 20.0));
    assertEquals(
        Optional.of(Identifier.of("nightborn", pick)),
        VeteranChoice.pick(
            attacks,
            Map.of(Identifier.of("nightborn", "chain"), chainReady),
            now,
            at(10.0, false, false, 1, 0)));
  }

  @ParameterizedTest(name = "moved {0}: dodged {1}")
  @CsvSource({"0.5, false", "3.0, false", "3.1, true", "12.0, true"})
  void countsATeleportDuringTheWindUpAsADodge(final double moved, final boolean dodged) {
    assertEquals(dodged, VeteranChoice.dodged(moved));
  }

  @ParameterizedTest(name = "still {0}, moved {1}: still {2}")
  @CsvSource({"0, 0.0, 1", "19, 0.01, 20", "19, 0.05, 0", "40, 2.0, 0"})
  void countsTicksStandingStill(final int before, final double moved, final int after) {
    assertEquals(after, VeteranChoice.stillTicks(before, moved));
  }

  @ParameterizedTest(name = "dodged {0}, alive {1}, vanished {2}, sees {3}, {4} of {5}: lands {6}")
  @CsvSource({
    "false, true, false, true, 6.0, 12.0, true",
    "true, true, false, true, 6.0, 12.0, false",
    "false, false, false, true, 6.0, 12.0, false",
    "false, true, true, true, 6.0, 12.0, false",
    "false, true, false, false, 6.0, 12.0, false",
    "false, true, false, true, 12.5, 12.0, false"
  })
  void landsTheChainOnlyOnAVampireStillInSightAndReach(
      final boolean dodged,
      final boolean alive,
      final boolean vanished,
      final boolean sees,
      final double distance,
      final double reach,
      final boolean lands) {
    assertEquals(lands, VeteranChoice.chainLands(dodged, alive, vanished, sees, distance, reach));
  }

  @ParameterizedTest(name = "standing {0}/{1}, unseen {2}, fallen {3}: {4}")
  @CsvSource({
    "192.0, 192.0, 0, 0, 1.0",
    "24.0, 24.0, 0, 7, 0.125",
    "48.0, 96.0, 0, 0, 0.5",
    "24.0, 24.0, 1, 0, 1.0",
    "0.0, 0.0, 0, 0, 1.0"
  })
  void measuresThePartysHealthCountingTheFallen(
      final double health,
      final double max,
      final int unseen,
      final int fallen,
      final double share) {
    assertEquals(share, VeteranChoice.partyHealth(health, max, unseen, fallen, 24.0), 1.0E-9);
  }
}
