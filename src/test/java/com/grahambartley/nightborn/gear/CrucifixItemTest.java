package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CrucifixItemTest {

  static Stream<Arguments> crucifixes() {
    return Stream.of(
        Arguments.of(GearItems.GOLD_CRUCIFIX, GearBlocks.GOLD_WALL_CRUCIFIX, "gold_crucifix"),
        Arguments.of(
            GearItems.SILVER_CRUCIFIX, GearBlocks.SILVER_WALL_CRUCIFIX, "silver_crucifix"));
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("crucifixes")
  void keepsItsOwnNameWhileHangingItsWallBlock(
      final Item crucifix, final Block wall, final String id) {
    assertEquals("item.nightborn." + id, crucifix.getTranslationKey());
    assertEquals(wall, ((CrucifixItem) crucifix).getBlock());
  }
}
