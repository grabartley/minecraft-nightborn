package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.servant.job.Bearing;
import com.grahambartley.nightborn.servant.job.ScoutTrip;
import com.grahambartley.nightborn.structure.NearestStructures.Found;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildRumourTest {

  @ParameterizedTest(name = "{0} at ({1}, {2}) -> {3} blocks {4}")
  @CsvSource({
    "crypt, 600, 800, 1000, SOUTH_EAST",
    "castle, 0, -300, 300, NORTH",
    "underground_lair, -50, 0, 50, WEST"
  })
  @DisplayName("the Guild Master names the nearest house and tells how far, which way and where")
  void told(final String kind, final int x, final int z, final int blocks, final Bearing bearing) {
    final Identifier structure = Identifier.of("nightborn", kind);
    final Found house = new Found(structure, new BlockPos(x, 64, z));
    final TranslatableTextContent tale =
        (TranslatableTextContent)
            GuildRumour.tale(Text.literal("Aldous"), BlockPos.ORIGIN, Optional.of(house))
                .getContent();
    assertEquals("nightborn.guild.rumour.told", tale.getKey());
    assertEquals(ScoutTrip.placeName(structure), tale.getArgs()[1]);
    assertEquals(blocks, tale.getArgs()[2]);
    assertEquals(Text.translatable(bearing.translationKey()), tale.getArgs()[3]);
    assertEquals(x, tale.getArgs()[4]);
    assertEquals(z, tale.getArgs()[5]);
  }

  @Test
  @DisplayName("with no house in reach he says he has heard of none")
  void none() {
    final TranslatableTextContent tale =
        (TranslatableTextContent)
            GuildRumour.tale(Text.literal("Aldous"), BlockPos.ORIGIN, Optional.empty())
                .getContent();
    assertEquals("nightborn.guild.rumour.none", tale.getKey());
    assertEquals("nightborn:vampire_houses", GuildRumour.VAMPIRE_HOUSES.id().toString());
  }
}
