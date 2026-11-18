package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CampClaimsTest {

  private static final BlockPos AT = new BlockPos(3, 70, -2);

  private static BlockState block(final String name) {
    return switch (name) {
      case "wool" -> Blocks.BROWN_WOOL.getDefaultState();
      case "bed" -> Blocks.BROWN_BED.getDefaultState();
      default -> Blocks.STONE.getDefaultState();
    };
  }

  @ParameterizedTest(name = "a claimed {0} broken as {1} drops nothing: {2}")
  @CsvSource({"wool, wool, true", "bed, bed, true", "wool, stone, false"})
  void onlyTheClaimedBlockDropsNothing(
      final String claimed, final String broken, final boolean silent) {
    final CampClaims claims = new CampClaims();
    claims.claim(AT, block(claimed));
    assertEquals(silent, claims.holds(AT, block(broken)));
    assertEquals(silent, claims.release(AT, block(broken)));
    assertEquals(0, claims.size());
  }

  @Test
  void anUnclaimedBlockDropsAsUsual() {
    assertEquals(false, new CampClaims().release(AT, block("wool")));
  }

  @Test
  void claimsSurviveASaveAndLoad() {
    final CampClaims claims = new CampClaims();
    claims.claim(AT, block("bed"));
    final NbtCompound saved = claims.writeNbt(new NbtCompound(), null);
    assertEquals(true, CampClaims.fromNbt(saved, null).holds(AT, block("bed")));
  }
}
