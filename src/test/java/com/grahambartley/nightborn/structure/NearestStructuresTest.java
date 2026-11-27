package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.structure.NearestStructures.Found;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class NearestStructuresTest {

  static final List<Found> CRYPT =
      List.of(new Found(Identifier.of("nightborn", "crypt"), new BlockPos(400, 0, 0)));

  @ParameterizedTest(name = "searcher stopped {0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("a search answers on the search thread, or fails as a future once it has stopped")
  void search(final boolean stopped) {
    final ExecutorService searcher = Executors.newSingleThreadExecutor();
    if (stopped) {
      searcher.shutdownNow();
    }
    final var answer = NearestStructures.search(searcher, () -> CRYPT);
    if (stopped) {
      assertTrue(answer.isCompletedExceptionally());
    } else {
      assertEquals(CRYPT, answer.join());
    }
    searcher.shutdownNow();
  }

  @ParameterizedTest(name = "range {0}, vampire house {1} -> searched out to {2}")
  @CsvSource({"500, true, 644", "500, false, 500", "3000, true, 3144", "3000, false, 3000"})
  @DisplayName(
      "a vampire house is looked for as far past the range as its entrance can lie from its locate"
          + " point; anything else is looked for within the range")
  void reach(final int range, final boolean house, final int reach) {
    assertEquals(reach, NearestStructures.reach(range, house));
  }
}
