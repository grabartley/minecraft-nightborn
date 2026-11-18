package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ThrallRegistryTest {

  private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @ParameterizedTest(name = "{0} thralls")
  @ValueSource(ints = {0, 1, 3})
  @DisplayName("an owner's thralls are counted, saved and loaded back")
  void roundTrips(final int count) {
    final ThrallRegistry registry = new ThrallRegistry();
    for (int i = 0; i < count; i++) {
      registry.bind(OWNER, UUID.randomUUID());
    }
    final ThrallRegistry loaded =
        ThrallRegistry.fromNbt(registry.writeNbt(new NbtCompound(), null), null);
    assertEquals(count, loaded.count(OWNER));
    assertEquals(0, loaded.count(OTHER));
  }

  @Test
  @DisplayName("a dead thrall leaves its owner's count and no one else's")
  void frees() {
    final ThrallRegistry registry = new ThrallRegistry();
    final UUID kept = UUID.randomUUID();
    final UUID lost = UUID.randomUUID();
    registry.bind(OWNER, kept);
    registry.bind(OWNER, lost);
    registry.free(OTHER, kept);
    registry.free(OWNER, lost);
    assertTrue(registry.keeps(OWNER, kept));
    assertFalse(registry.keeps(OWNER, lost));
    assertEquals(1, registry.count(OWNER));
  }

  @Test
  @DisplayName("freeing an owner lets every one of their thralls go")
  void freesOwner() {
    final ThrallRegistry registry = new ThrallRegistry();
    registry.bind(OWNER, UUID.randomUUID());
    registry.bind(OWNER, UUID.randomUUID());
    assertEquals(2, registry.freeOwner(OWNER));
    assertEquals(0, registry.freeOwner(OWNER));
    assertEquals(0, registry.count(OWNER));
  }

  @ParameterizedTest(name = "{0} thralls")
  @ValueSource(ints = {0, 1, 3})
  @DisplayName("an owner's thralls can be listed")
  void lists(final int count) {
    final ThrallRegistry registry = new ThrallRegistry();
    for (int i = 0; i < count; i++) {
      registry.bind(OWNER, UUID.randomUUID());
    }
    registry.bind(UUID.randomUUID(), UUID.randomUUID());
    assertEquals(count, registry.thralls(OWNER).size());
  }
}
