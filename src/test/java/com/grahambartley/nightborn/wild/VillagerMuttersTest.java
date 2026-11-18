package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillagerMuttersTest {

  @ParameterizedTest(name = "line {0} about a house at {1}, {2} -> {3} {4}")
  @CsvSource({
    "0, 0, -100, nightborn.villager.mutter.0, nightborn.bearing.north",
    "1, 100, 0, nightborn.villager.mutter.1, nightborn.bearing.east",
    "3, 0, 100, nightborn.villager.mutter.3, nightborn.bearing.south",
    "5, -100, -100, nightborn.villager.mutter.1, nightborn.bearing.north_west"
  })
  @DisplayName("a villager mutters one of its lines and the way the house lies from it")
  void mutter(final int line, final int x, final int z, final String key, final String bearing) {
    final Text said =
        VillagerMutters.mutter(Text.literal("Ada"), BlockPos.ORIGIN, new BlockPos(x, 64, z), line);
    final TranslatableTextContent content = (TranslatableTextContent) said.getContent();
    assertEquals(key, content.getKey());
    assertEquals("Ada", ((Text) content.getArgs()[0]).getString());
    assertEquals(
        bearing, ((TranslatableTextContent) ((Text) content.getArgs()[1]).getContent()).getKey());
  }
}
