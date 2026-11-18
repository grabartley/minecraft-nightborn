package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.coating.Coating;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BanesTest {

  private static final UUID HUNTER = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @ParameterizedTest(name = "stake {0}, silver {1} -> {2}")
  @CsvSource({
    "false, false, ''",
    "true, false, STAKE",
    "false, true, SILVER",
    "true, true, SILVER STAKE"
  })
  void aBlowNamesTheBanesItCarries(final boolean stake, final boolean silver, final String banes) {
    assertEquals(parse(banes), Banes.of(stake, silver));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"GARLIC, GARLIC", "HOLY_WATER, HOLY_WATER"})
  void aCoatingIsItsOwnBane(final Coating coating, final Banes.Bane bane) {
    assertEquals(Set.of(bane), Banes.of(coating));
  }

  @ParameterizedTest(name = "{0} then {1} -> {2}")
  @CsvSource({
    "GARLIC, GARLIC, 1",
    "GARLIC, SILVER, 2",
    "GARLIC HOLY_WATER, SILVER STAKE, 4",
    "STAKE, '', 1"
  })
  void marksGatherPerAttacker(final String first, final String second, final int count) {
    final Banes.Marks marks =
        Banes.Marks.NONE.with(HUNTER, parse(first)).with(HUNTER, parse(second));
    assertEquals(count, marks.of(HUNTER).size());
    assertEquals(0, marks.of(OTHER).size());
  }

  private static Set<Banes.Bane> parse(final String banes) {
    final Set<Banes.Bane> parsed = EnumSet.noneOf(Banes.Bane.class);
    for (final String bane : banes.split(" ")) {
      if (!bane.isEmpty()) {
        parsed.add(Banes.Bane.valueOf(bane));
      }
    }
    return parsed;
  }
}
