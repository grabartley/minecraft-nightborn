package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Set;
import net.minecraft.block.BlockState;
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
}
