package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.faction.Oath;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankLadderTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({"'master, initiate, veteran, hunter'", "'hunter, master, initiate, veteran'"})
  @DisplayName("the ladder runs by order whatever order the data loads in")
  void runsByOrder(final String loaded) {
    final Map<Identifier, GuildRank> ranks = new HashMap<>();
    for (final String path : loaded.split(", ")) {
      final RankLadder.Rung rung =
          RankFixtures.LADDER.rungs().get(RankFixtures.LADDER.rankOf(id(path)).getAsInt());
      ranks.put(rung.id(), rung.rank());
    }
    assertEquals(RankFixtures.LADDER, RankLadder.of(ranks));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"initiate, 0", "hunter, 1", "master, 3", "lost, -1"})
  @DisplayName("a rank id finds its place on the ladder, and an unknown one finds none")
  void rankOf(final String path, final int rank) {
    assertEquals(
        rank < 0 ? OptionalInt.empty() : OptionalInt.of(rank),
        RankFixtures.LADDER.rankOf(id(path)));
  }

  @ParameterizedTest(name = "proven {0}, sworn {1}, rank {2} -> {3}")
  @CsvSource({
    "false, false, 0, ''",
    "true, false, 0, ''",
    "true, true, 0, initiate",
    "true, true, 2, veteran",
    "true, true, 9, ''"
  })
  @DisplayName("only one who took the oath bears a mark, and it names their rank")
  void markOf(final boolean proven, final boolean sworn, final int rank, final String mark) {
    assertEquals(
        mark.isEmpty() ? Optional.empty() : Optional.of(id(mark)),
        RankFixtures.LADDER.markOf(new Oath(proven, sworn, rank, 0)));
  }

  @ParameterizedTest(name = "rank {0} has a deed {1}")
  @CsvSource({"0, false", "1, true", "3, true", "4, false"})
  @DisplayName("every rank above Initiate has a deed")
  void deeds(final int rank, final boolean deed) {
    assertEquals(deed, RankFixtures.LADDER.deed(rank).isPresent());
  }

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }
}
