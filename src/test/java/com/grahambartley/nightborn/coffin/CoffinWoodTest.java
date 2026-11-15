package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoffinWoodTest {

  static Stream<Arguments> woods() {
    return Stream.of(
        Arguments.of(CoffinWood.OAK, "oak_coffin", Blocks.OAK_PLANKS, true),
        Arguments.of(CoffinWood.SPRUCE, "spruce_coffin", Blocks.SPRUCE_PLANKS, true),
        Arguments.of(CoffinWood.DARK_OAK, "dark_oak_coffin", Blocks.DARK_OAK_PLANKS, true),
        Arguments.of(CoffinWood.MANGROVE, "mangrove_coffin", Blocks.MANGROVE_PLANKS, true),
        Arguments.of(CoffinWood.CHERRY, "cherry_coffin", Blocks.CHERRY_PLANKS, true),
        Arguments.of(CoffinWood.CRIMSON, "crimson_coffin", Blocks.CRIMSON_PLANKS, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("each wood names its coffin, its planks and whether it burns")
  void describesTheWood(
      final CoffinWood wood, final String id, final Block planks, final boolean flammable) {
    assertEquals(id, wood.id());
    assertEquals(planks, wood.planks());
    assertEquals(flammable, wood.flammable());
  }
}
