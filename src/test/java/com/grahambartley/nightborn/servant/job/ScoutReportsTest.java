package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScoutReportsTest {

  @ParameterizedTest(name = "{0} villagers, {1} hunters, {2} vampires, {3} humans")
  @CsvSource({"0, 0, 0, 0", "5, 2, 1, 0", "12, 0, 3, 2"})
  @DisplayName("a report names the scout, the place and every count it saw")
  void report(final int villagers, final int hunters, final int vampires, final int humans) {
    final ScoutTrip trip =
        ScoutTrip.to(BlockPos.ORIGIN, Identifier.of("nightborn", "crypt"))
            .reported(new ScoutReport(villagers, hunters, vampires, humans));
    final TranslatableTextContent said =
        (TranslatableTextContent) ScoutReports.text(Text.literal("Hester"), trip).getContent();
    assertEquals("nightborn.scout.report", said.getKey());
    assertEquals(Text.literal("Hester"), said.getArgs()[0]);
    assertEquals(trip.placeName(), said.getArgs()[1]);
    assertEquals(villagers, said.getArgs()[2]);
    assertEquals(hunters, said.getArgs()[3]);
    assertEquals(vampires, said.getArgs()[4]);
    assertEquals(humans, said.getArgs()[5]);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"nightborn:crypt", "minecraft:village_desert"})
  @DisplayName("a scout that lost its way says so instead of making a report up")
  void lost(final String place) {
    final ScoutTrip trip = ScoutTrip.to(BlockPos.ORIGIN, Identifier.of(place)).lostTheWay();
    final TranslatableTextContent said =
        (TranslatableTextContent) ScoutReports.text(Text.literal("Hester"), trip).getContent();
    assertEquals("nightborn.scout.lost", said.getKey());
    assertEquals(2, said.getArgs().length);
  }
}
