package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HeldRanksTest {

  static final Identifier BRANCH = Identifier.of("nightborn", "trapper");
  static final Identifier THREE = Identifier.of("nightborn", "trapper/field_craft");
  static final Identifier ONE = Identifier.of("nightborn", "trapper/snare");
  static final Identifier GONE = Identifier.of("nightborn", "trapper/removed");

  static Skill skill(final int... costs) {
    return new Skill(
        BRANCH,
        List.of(),
        Identifier.of("nightborn", "skills/icon/snare"),
        0,
        0,
        Optional.empty(),
        Optional.empty(),
        java.util.Arrays.stream(costs).mapToObj(SkillRank::costing).toList(),
        Optional.empty());
  }

  @ParameterizedTest(name = "{0} ranks of a three-rank skill, snare {1}, removed skill {2} -> {3}")
  @CsvSource({
    "0, false, false, ''",
    "1, false, false, '1'",
    "3, false, false, '1,2,3'",
    "2, true, false, '1,2,4'",
    "3, true, true, '1,2,3,4'"
  })
  @DisplayName(
      "a holder holds each rank bought of each owned skill, and skips skills that no longer exist")
  void held(final int ranks, final boolean snare, final boolean removed, final String costs) {
    TreeProgress owned = TreeProgress.EMPTY.withEarned(60);
    for (int i = 0; i < ranks; i++) {
      owned = owned.buy(THREE, 1, null);
    }
    if (snare) {
      owned = owned.buy(ONE, 1, null);
    }
    if (removed) {
      owned = owned.buy(GONE, 1, null);
    }
    final Map<Identifier, Skill> skills = Map.of(THREE, skill(1, 2, 3), ONE, skill(4));
    assertEquals(
        costs,
        String.join(
            ",",
            HeldRanks.held(owned, skills::get).stream()
                .map(rank -> Integer.toString(rank.cost()))
                .toList()));
  }
}
