package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ChaliceBlockTest {

  static Stream<Arguments> contents() {
    return Stream.of(
        Arguments.of(ChaliceContents.EMPTY),
        Arguments.of(new ChaliceContents(ChaliceBlood.ANIMAL, 1)),
        Arguments.of(new ChaliceContents(ChaliceBlood.VILLAGER, 4)),
        Arguments.of(new ChaliceContents(ChaliceBlood.PLAYER, 7)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("contents")
  void keepsItsContentsInItsState(final ChaliceContents contents) {
    assertEquals(
        contents,
        ChaliceBlock.contents(ChaliceBlock.with(DecorBlocks.CHALICE.getDefaultState(), contents)));
  }

  @Test
  void isPlacedEmpty() {
    assertEquals(
        ChaliceContents.EMPTY, ChaliceBlock.contents(DecorBlocks.CHALICE.getDefaultState()));
  }

  @Test
  void aChaliceFromAnOlderWorldKeepsTheBottleItHeld() {
    assertEquals(
        new ChaliceContents(ChaliceBlood.ANIMAL, 1),
        ChaliceBlock.contents(
            DecorBlocks.CHALICE.getDefaultState().with(ChaliceBlock.BLOOD, ChaliceBlood.ANIMAL)));
  }
}
