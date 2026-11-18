package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.servant.job.Bearing;
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

  @ParameterizedTest(name = "house at ({0}, {1}) -> {2} blocks {3}")
  @CsvSource({"600, 800, 1000, SOUTH_EAST", "0, -300, 300, NORTH", "-50, 0, 50, WEST"})
  @DisplayName("the Guild Master tells how far the nearest house lies and which way")
  void told(final int x, final int z, final int blocks, final Bearing bearing) {
    final Found house = new Found(Identifier.of("nightborn", "crypt"), new BlockPos(x, 0, z));
    final TranslatableTextContent tale =
        (TranslatableTextContent)
            GuildRumour.tale(Text.literal("Aldous"), BlockPos.ORIGIN, Optional.of(house))
                .getContent();
    assertEquals("nightborn.guild.rumour.told", tale.getKey());
    assertEquals(blocks, tale.getArgs()[1]);
    assertEquals(Text.translatable(bearing.translationKey()), tale.getArgs()[2]);
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
