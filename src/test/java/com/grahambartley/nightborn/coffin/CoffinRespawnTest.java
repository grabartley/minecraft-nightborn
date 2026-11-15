package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinRespawnTest {

  private static final UUID PLAYER = new UUID(0, 1);
  private static final UUID OTHER = new UUID(0, 2);
  private static final BlockPos HEAD = new BlockPos(3, 70, -9);
  private static final GlobalPos COFFIN = GlobalPos.create(World.OVERWORLD, HEAD);

  static Stream<Arguments> respawns() {
    return Stream.of(
        Arguments.of("a respawn point that is not a coffin", false, Optional.empty(), true),
        Arguments.of("the player's own coffin", true, Optional.of(PLAYER), true),
        Arguments.of("an unassigned coffin", true, Optional.empty(), false),
        Arguments.of("another vampire's coffin", true, Optional.of(OTHER), false));
  }

  @ParameterizedTest(name = "{0}: kept {3}")
  @MethodSource("respawns")
  @DisplayName("a coffin is a respawn point only for its owner, and other respawn points stand")
  void keepsARespawnOnlyInYourOwnCoffin(
      final String name, final boolean coffin, final Optional<UUID> owner, final boolean kept) {
    assertEquals(kept, CoffinRespawn.holds(coffin, owner, PLAYER));
  }

  static Stream<Arguments> points() {
    return Stream.of(
        Arguments.of("the freed coffin", World.OVERWORLD, HEAD, true),
        Arguments.of("the same spot in another dimension", World.NETHER, HEAD, false),
        Arguments.of("another spot", World.OVERWORLD, HEAD.up(), false),
        Arguments.of("no respawn point", World.OVERWORLD, null, false));
  }

  @ParameterizedTest(name = "{0}: forgotten {3}")
  @MethodSource("points")
  @DisplayName("freeing a coffin forgets only a respawn point in that coffin")
  void forgetsOnlyTheFreedCoffin(
      final String name,
      final RegistryKey<World> dimension,
      final BlockPos pos,
      final boolean forgotten) {
    assertEquals(forgotten, CoffinRespawn.pointsAt(dimension, pos, COFFIN));
  }
}
