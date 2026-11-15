package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinecraftBootstrapExtension.class)
class ChandelierBlockTest {

  private final ChandelierBlock block = (ChandelierBlock) DecorBlocks.get("chandelier");

  @Test
  void everyWickSitsAtTheBaseOfAFlameInTheModel() throws IOException {
    assertEquals(GeoFlames.basesIn(this.block.artModel()), Set.copyOf(this.block.wicks()));
  }

  @Test
  void usesItsOwnArt() {
    assertEquals("chandelier", this.block.artModel());
  }
}
