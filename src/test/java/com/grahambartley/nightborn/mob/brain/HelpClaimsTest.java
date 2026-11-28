package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HelpClaimsTest {

  private static World world(final RegistryKey<World> key) {
    final World world = mock(World.class);
    when(world.getRegistryKey()).thenReturn(key);
    return world;
  }

  @ParameterizedTest(name = "asked in the same world {0}: claimed {1}")
  @CsvSource({"true, true", "false, false"})
  @DisplayName("a claim holds in the world it was made in and nowhere else")
  void claimsAreKeptPerWorld(final boolean sameWorld, final boolean claimed) {
    final UUID ally = UUID.randomUUID();
    HelpClaims.claim(world(World.OVERWORLD), ally, new UUID(0, 1), 50);
    assertEquals(
        claimed,
        HelpClaims.in(world(sameWorld ? World.OVERWORLD : World.NETHER))
            .claimedByOther(ally, new UUID(0, 2), 40));
  }
}
