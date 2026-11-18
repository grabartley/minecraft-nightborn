package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KeptDeedsTest {

  @ParameterizedTest(name = "owning [{0}]")
  @CsvSource({
    "''",
    "nightborn:ring_of_the_court",
    "nightborn:ring_of_the_court nightborn:gold_band"
  })
  void heldAccessoriesAreCountedAndNamed(final String owned) {
    final List<Identifier> accessories =
        Arrays.stream(owned.split(" ")).filter(id -> !id.isEmpty()).map(Identifier::of).toList();
    final Optional<Deed> deed = KeptDeeds.held(accessories);
    assertEquals(accessories.isEmpty(), deed.isEmpty());
    deed.ifPresent(
        held -> {
          assertEquals(DeedKinds.ACCESSORIES_HELD, held.kind());
          assertEquals(accessories.size(), held.amount());
          accessories.forEach(id -> assertEquals(Optional.of("true"), held.fact(id.toString())));
        });
  }

  @ParameterizedTest(name = "{0} slots worn")
  @CsvSource({"0, false", "1, true", "8, true"})
  void wornSlotsAreCounted(final int slots, final boolean recorded) {
    final Optional<Deed> deed = KeptDeeds.worn(slots);
    assertEquals(recorded, deed.isPresent());
    deed.ifPresent(worn -> assertEquals(slots, worn.number(DeedKinds.SLOTS).getAsInt()));
  }

  @ParameterizedTest(name = "thralls {0}, ghosts {1}, human familiar {2} -> {3} kinds")
  @CsvSource({
    "0, 0, false, 0",
    "3, 0, false, 1",
    "1, 2, false, 2",
    "0, 1, true, 2",
    "2, 1, true, 3"
  })
  void staffCountsEachKindKeptOnce(
      final int thralls, final int ghosts, final boolean human, final int kinds) {
    final Optional<Deed> deed = KeptDeeds.staff(thralls, ghosts, human);
    assertEquals(kinds > 0, deed.isPresent());
    deed.ifPresent(staff -> assertEquals(kinds, staff.number(DeedKinds.KINDS).getAsInt()));
  }

  @ParameterizedTest(name = "vampire {0}, main {1}, off {2} -> {3}")
  @CsvSource({
    "false, true, true, true",
    "false, true, false, false",
    "false, false, true, false",
    "true, true, true, false"
  })
  void onlyAHumanWithACrucifixInEachHandCounts(
      final boolean vampire, final boolean main, final boolean off, final boolean expected) {
    assertEquals(expected, KeptDeeds.crucifixes(vampire, main, off).isPresent());
  }
}
