package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DrapeBlockTest {

  @ParameterizedTest(name = "{0} with top={1} hides the rod: {2}")
  @CsvSource({
    "burgundy_velvet_drape, true, false",
    "burgundy_velvet_drape, false, true",
    "emerald_velvet_drape, true, false",
    "emerald_velvet_drape, false, true"
  })
  void showsTheRodOnlyOnTheTopDrapeOfAColumn(
      final String id, final boolean top, final boolean rodHidden) {
    final DrapeBlock drape = (DrapeBlock) DecorBlocks.get(id);
    final BlockState state = drape.getDefaultState().with(DrapeBlock.TOP, top);
    assertEquals(rodHidden ? Set.of(DrapeBlock.ROD_BONE) : Set.of(), drape.hiddenBones(state));
  }

  @ParameterizedTest(name = "{0} animates on open and shares the drape art")
  @CsvSource({"burgundy_velvet_drape", "emerald_velvet_drape"})
  void animatesOnOpenWithTheSharedDrapeModel(final String id) {
    final DrapeBlock drape = (DrapeBlock) DecorBlocks.get(id);
    assertEquals(DrapeBlock.OPEN, drape.toggleProperty());
    assertEquals("velvet_drape", drape.artModel());
  }

  @ParameterizedTest(name = "{0} turns north to {1}")
  @CsvSource({
    "NONE, NORTH",
    "CLOCKWISE_90, EAST",
    "CLOCKWISE_180, SOUTH",
    "COUNTERCLOCKWISE_90, WEST"
  })
  void turnsWithTheStructureItIsPlacedIn(final BlockRotation rotation, final Direction expected) {
    assertEquals(
        expected,
        DecorBlocks.BURGUNDY_VELVET_DRAPE
            .getDefaultState()
            .rotate(rotation)
            .get(DrapeBlock.FACING));
  }

  @ParameterizedTest(name = "{0} mirrors north to {1}")
  @CsvSource({"NONE, NORTH", "LEFT_RIGHT, SOUTH", "FRONT_BACK, NORTH"})
  void mirrorsWithTheStructureItIsPlacedIn(final BlockMirror mirror, final Direction expected) {
    assertEquals(
        expected,
        DecorBlocks.EMERALD_VELVET_DRAPE.getDefaultState().mirror(mirror).get(DrapeBlock.FACING));
  }
}
