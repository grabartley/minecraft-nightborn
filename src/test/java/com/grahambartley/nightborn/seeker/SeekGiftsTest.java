package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeekGiftsTest {

  private static List<SeekGift> keen(final int ranks, final boolean trails) {
    final List<SeekGift> gifts = new ArrayList<>();
    for (int rank = 1; rank <= ranks; rank++) {
      gifts.add(new SeekGift(32, 100, trails && rank == ranks, 0.0F, 0.0F, 0.0));
    }
    return gifts;
  }

  @ParameterizedTest(name = "{0} ranks -> range {1}, time {2}, trails {3}")
  @CsvSource({
    "0, false, 64, 200, false",
    "1, false, 96, 300, false",
    "2, false, 128, 400, false",
    "3, true, 160, 500, true"
  })
  @DisplayName("each rank of Keen Tracking reaches 32 blocks further and lasts 5 seconds longer")
  void keenTracking(
      final int ranks,
      final boolean trails,
      final int range,
      final int ticks,
      final boolean shows) {
    final List<SeekGift> gifts = keen(ranks, trails);
    assertEquals(range, SeekGifts.trackRange(64, gifts));
    assertEquals(ticks, SeekGifts.trackTicks(200, gifts));
    assertEquals(shows, SeekGifts.bloodTrails(gifts));
  }

  @ParameterizedTest(name = "sense {0} and {1}, listen {2}, allied {3} and {4}")
  @CsvSource({
    "0, 0, 0, 0, 0, 0, 0, 0",
    "48, 0, 12, 0.1, 0, 48, 12, 0.1",
    "48, 24, 0, 0.1, 0.1, 48, 0, 0.2"
  })
  @DisplayName("the widest sense and hearing count, and allied shares add up")
  void senses(
      final float senseA,
      final float senseB,
      final float listen,
      final double alliedA,
      final double alliedB,
      final float sense,
      final float hears,
      final double allied) {
    final List<SeekGift> gifts =
        List.of(
            new SeekGift(0, 0, false, senseA, listen, alliedA),
            new SeekGift(0, 0, false, senseB, 0.0F, alliedB));
    assertEquals(sense, SeekGifts.senseRange(gifts));
    assertEquals(hears, SeekGifts.listenRange(gifts));
    assertEquals(allied, SeekGifts.alliedShare(gifts), 1.0e-9);
  }
}
