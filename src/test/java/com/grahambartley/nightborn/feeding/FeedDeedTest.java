package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FeedDeedTest {

  @ParameterizedTest(name = "{0}, animal {1}, villager {2}, job {3}, seen {4}, gift {5}")
  @CsvSource({
    "minecraft:cow, true, false, , false, , false",
    "minecraft:horse, true, false, , false, nightborn:horse, true",
    "minecraft:villager, false, true, minecraft:cleric, true, , false",
    "minecraft:villager, false, true, , false, nightborn:villager, true"
  })
  void tellsWhatWasFedOnAndHow(
      final String prey,
      final boolean animal,
      final boolean villager,
      final String profession,
      final boolean seen,
      final String gift,
      final boolean warded) {
    final Deed deed =
        FeedDeed.of(
            Identifier.of(prey),
            animal,
            villager,
            Optional.ofNullable(profession).map(Identifier::of),
            seen,
            Optional.ofNullable(gift).map(Identifier::of),
            warded);
    assertEquals(DeedKinds.FED, deed.kind());
    assertEquals(Optional.of(prey), deed.fact(DeedKinds.PREY));
    assertEquals(Optional.of(Boolean.toString(animal)), deed.fact(DeedKinds.ANIMAL));
    assertEquals(Optional.of(Boolean.toString(villager)), deed.fact(DeedKinds.VILLAGER));
    assertEquals(Optional.ofNullable(profession), deed.fact(DeedKinds.PROFESSION));
    assertEquals(Optional.of(Boolean.toString(seen)), deed.fact(DeedKinds.SEEN));
    assertEquals(Optional.of(Boolean.toString(gift != null)), deed.fact(DeedKinds.GIFTED));
    assertEquals(Optional.ofNullable(gift), deed.fact(DeedKinds.GIFT));
    assertEquals(Optional.of(Boolean.toString(warded)), deed.fact(DeedKinds.WARDED));
  }
}
