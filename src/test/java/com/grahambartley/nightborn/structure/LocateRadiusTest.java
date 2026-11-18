package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Optional;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LocateRadiusTest {

  static StructurePlacement spread(final int spacing) {
    return new RandomSpreadStructurePlacement(spacing, spacing / 2, SpreadType.LINEAR, 1);
  }

  @ParameterizedTest(name = "{0} blocks -> {1} chunks")
  @CsvSource({"0, 1", "1, 1", "16, 1", "17, 2", "256, 16", "1024, 64"})
  @DisplayName("a range in blocks searches enough chunks to cover it, and never none")
  void chunks(final int blocks, final int chunks) {
    assertEquals(chunks, LocateRadius.chunks(blocks));
  }

  @ParameterizedTest(name = "{0} blocks, spacing {1} -> {2} rings")
  @CsvSource({
    "1024, 48, 2",
    "1024, 34, 2",
    "1024, 64, 1",
    "1024, 1, 64",
    "768, 48, 1",
    "769, 48, 2",
    "16, 48, 1",
    "4096, 48, 6"
  })
  @DisplayName(
      "a spread structure is searched in rings of placement cells that just cover the range")
  void rings(final int blocks, final int spacing, final int rings) {
    assertEquals(rings, LocateRadius.of(blocks, List.of(spread(spacing))));
  }

  @Test
  @DisplayName("with several spreads the tightest spacing sets the rings, so none is missed")
  void tightest() {
    assertEquals(4, LocateRadius.of(1024, List.of(spread(48), spread(16), spread(32))));
  }

  @Test
  @DisplayName("placements that are not spreads keep the range in chunks")
  void notSpread() {
    final StructurePlacement rings =
        new ConcentricRingsStructurePlacement(
            Vec3i.ZERO,
            StructurePlacement.FrequencyReductionMethod.DEFAULT,
            1.0F,
            0,
            Optional.empty(),
            32,
            3,
            128,
            RegistryEntryList.of());
    assertEquals(64, LocateRadius.of(1024, List.of(rings)));
    assertEquals(64, LocateRadius.of(1024, List.of()));
  }
}
