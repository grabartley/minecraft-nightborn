package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.util.stream.Collectors;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ChandelierBlockTest {

  private final ChandelierBlock block = (ChandelierBlock) DecorBlocks.get("chandelier");

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  void everyWickSitsAtTheBaseOfAFlameInTheModel(final Direction facing) throws IOException {
    assertEquals(
        GeoFlames.basesIn(this.block.artModel()).stream()
            .map(flame -> FacingShapes.turn(flame, facing))
            .collect(Collectors.toSet()),
        this.block.wicks().stream()
            .map(wick -> FacingShapes.turn(wick, facing))
            .collect(Collectors.toSet()));
  }
}
