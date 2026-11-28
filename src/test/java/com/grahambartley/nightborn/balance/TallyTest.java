package com.grahambartley.nightborn.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TallyTest {

  @ParameterizedTest(name = "melee {0} and {1}, other {2}: {3} melee hits, {4} other, share {5}")
  @CsvSource({
    "4.0, 2.0, 2.0, 2, 1, 0.75",
    "0.0, 0.0, 3.0, 0, 1, 0.0",
    "5.0, 5.0, 0.0, 2, 0, 1.0",
    "0.0, 0.0, 0.0, 0, 0, 0.0"
  })
  void hits(
      final float first,
      final float second,
      final float other,
      final int meleeHits,
      final int otherHits,
      final double share) {
    final Tally tally = new Tally();
    tally.hit(Side.HUNTERS, true, first);
    tally.hit(Side.HUNTERS, true, second);
    tally.hit(Side.HUNTERS, false, other);
    final Tally.Blows blows = tally.of(Side.HUNTERS);
    assertEquals(meleeHits, blows.meleeHits());
    assertEquals(otherHits, blows.otherHits());
    assertEquals(share, blows.meleeShare(), 1e-6);
    assertEquals(Tally.Blows.NONE, tally.of(Side.VAMPIRES));
  }

  @ParameterizedTest(name = "{0} plus {1} melee hits is {2}")
  @CsvSource({"1, 2, 3", "0, 0, 0"})
  void plus(final int one, final int other, final int sum) {
    final Tally.Blows a = new Tally.Blows(one, one, 0, 0.0F);
    final Tally.Blows b = new Tally.Blows(other, other, 1, 1.0F);
    assertEquals(sum, a.plus(b).meleeHits());
    assertEquals(sum + 1.0F, a.plus(b).damage(), 1e-6);
  }
}
