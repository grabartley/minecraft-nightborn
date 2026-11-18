package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.structure.NearestStructures.Found;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NearestStructureCacheTest {

  static NearestStructureCache.Ask ask(final long chunk, final int range, final long day) {
    return new NearestStructureCache.Ask(
        Identifier.ofVanilla("overworld"),
        Identifier.of("nightborn", "vampire_houses"),
        chunk,
        range,
        day);
  }

  static final List<Found> CRYPT =
      List.of(new Found(Identifier.of("nightborn", "crypt"), new BlockPos(400, 0, 0)));

  @ParameterizedTest(name = "chunk {0}, range {1}, day {2} -> recalled {3}")
  @CsvSource({"5, 256, 3, true", "6, 256, 3, false", "5, 512, 3, false", "5, 256, 4, false"})
  @DisplayName(
      "a search is reused only for the same chunk, range and day, so asking again is cheap")
  void recall(final long chunk, final int range, final long day, final boolean recalled) {
    final NearestStructureCache cache = new NearestStructureCache();
    cache.remember(ask(5, 256, 3), CRYPT);
    assertEquals(
        recalled ? Optional.of(CRYPT) : Optional.empty(), cache.recall(ask(chunk, range, day)));
  }

  @Test
  @DisplayName("the cache keeps only the most recent searches and empties when told")
  void bounded() {
    final NearestStructureCache cache = new NearestStructureCache();
    for (long chunk = 0; chunk <= NearestStructureCache.MOST_KEPT; chunk++) {
      cache.remember(ask(chunk, 256, 0), CRYPT);
    }
    assertTrue(cache.recall(ask(0, 256, 0)).isEmpty());
    assertTrue(cache.recall(ask(NearestStructureCache.MOST_KEPT, 256, 0)).isPresent());
    cache.forget();
    assertTrue(cache.recall(ask(NearestStructureCache.MOST_KEPT, 256, 0)).isEmpty());
  }
}
