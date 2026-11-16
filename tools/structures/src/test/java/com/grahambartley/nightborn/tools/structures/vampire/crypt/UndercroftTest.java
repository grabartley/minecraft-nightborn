package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UndercroftTest {

  private static final Template CRYPT = Undercroft.build();

  static Stream<Arguments> bays() {
    return java.util.Arrays.stream(Undercroft.BAYS_X)
        .boxed()
        .flatMap(x -> java.util.Arrays.stream(Undercroft.BAYS_Z).mapToObj(z -> Arguments.of(x, z)));
  }

  static Stream<Arguments> columns() {
    return Stream.of(Undercroft.COLUMNS).map(c -> Arguments.of(c[0], c[1]));
  }

  @Test
  void holdsFiveStoneCoffinsWhoseLidsCanSlideClear() {
    final List<Coffins.Coffin> coffins = Coffins.in(CRYPT);
    assertEquals(5, coffins.size());
    Coffins.assertSound(CRYPT, List.of(Undercroft.STONE_COFFIN, Undercroft.GOTHIC_COFFIN));
  }

  @ParameterizedTest(name = "the bay at {0},{1} rises to an open crown ringed by ribs")
  @MethodSource("bays")
  void everyBayIsVaulted(final int x, final int z) {
    final String crown = CRYPT.get(x, 4, z);
    assertTrue(crown.equals(BlockState.AIR) || crown.startsWith("nightborn:chandelier"), crown);
    assertTrue(
        CRYPT.get(x - 1, 4, z).startsWith("minecraft:" + CryptStair.RIB + "[facing=west,half=top"));
    assertTrue(
        CRYPT.get(x + 1, 4, z).startsWith("minecraft:" + CryptStair.RIB + "[facing=east,half=top"));
    assertTrue(
        CRYPT
            .get(x, 4, z - 1)
            .startsWith("minecraft:" + CryptStair.RIB + "[facing=north,half=top"));
    assertTrue(
        CRYPT
            .get(x, 4, z + 1)
            .startsWith("minecraft:" + CryptStair.RIB + "[facing=south,half=top"));
  }

  @ParameterizedTest(name = "the column at {0},{1} carries the vault")
  @MethodSource("columns")
  void columnsRunFromFloorToVault(final int x, final int z) {
    assertEquals("minecraft:" + CryptStair.CAPITAL, CRYPT.get(x, 1, z));
    assertTrue(CRYPT.get(x, 2, z).startsWith("minecraft:" + CryptStair.SHAFT));
    assertEquals("minecraft:" + CryptStair.CAPITAL, CRYPT.get(x, 3, z));
    assertEquals("minecraft:" + CryptStair.CAPITAL, CRYPT.get(x, 4, z));
  }

  @Test
  void theLandingOpensIntoTheHall() {
    assertEquals(BlockState.AIR, CRYPT.get(6, 1, 4));
    assertEquals(BlockState.AIR, CRYPT.get(6, 2, 3));
  }

  @Test
  void chandeliersHangFromTheVault() {
    Coffins.assertHung(CRYPT);
  }
}
