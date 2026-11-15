package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinecraftBootstrapExtension.class)
class CandelabraBlockTest {

  private final CandelabraBlock block = (CandelabraBlock) DecorBlocks.get("candelabra");

  @Test
  void everyWickSitsAtTheBaseOfAFlameInTheModel() throws IOException {
    assertEquals(GeoFlames.basesIn(this.block.artModel()), Set.copyOf(this.block.wicks()));
  }

  @Test
  void usesItsOwnArt() {
    assertEquals("candelabra", this.block.artModel());
  }
}
