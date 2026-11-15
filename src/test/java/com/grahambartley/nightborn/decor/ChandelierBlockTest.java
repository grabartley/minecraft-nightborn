package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ChandelierBlockTest {

  private static final Path GEO =
      Path.of("src/client/resources/assets/nightborn/geo/block/chandelier.geo.json");
  private static final double PIXEL = 16.0;

  private static ChandelierBlock block() {
    return (ChandelierBlock) DecorBlocks.get("chandelier");
  }

  @ParameterizedTest(name = "lit={0} gives light {1}")
  @CsvSource({"false, 0", "true, 15"})
  void lightsAndGlowsOnlyWhileLit(final boolean lit, final int light) {
    final BlockState state = block().getDefaultState().with(LightableFurnitureBlock.LIT, lit);
    assertEquals(light, state.getLuminance());
    assertEquals(lit, block().glows(state));
    assertEquals(LightableFurnitureBlock.LIT, block().toggleProperty());
    assertEquals("chandelier", block().artModel());
  }

  @Test
  void everyWickSitsAtTheBaseOfAFlameInTheModel() throws IOException {
    final Set<Vec3d> flames =
        StreamSupport.stream(
                JsonParser.parseString(Files.readString(GEO))
                    .getAsJsonObject()
                    .getAsJsonArray("minecraft:geometry")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonArray("bones")
                    .spliterator(),
                false)
            .map(JsonElement::getAsJsonObject)
            .filter(bone -> bone.get("name").getAsString().startsWith("flame_"))
            .map(bone -> bone.getAsJsonArray("pivot"))
            .map(
                pivot ->
                    new Vec3d(
                        (8 - pivot.get(0).getAsDouble()) / PIXEL,
                        pivot.get(1).getAsDouble() / PIXEL,
                        (8 + pivot.get(2).getAsDouble()) / PIXEL))
            .collect(Collectors.toSet());
    assertEquals(flames, Set.copyOf(block().wicks()));
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
        expected, block().getDefaultState().rotate(rotation).get(LightableFurnitureBlock.FACING));
  }

  @ParameterizedTest(name = "{0} mirrors north to {1}")
  @CsvSource({"NONE, NORTH", "LEFT_RIGHT, SOUTH", "FRONT_BACK, NORTH"})
  void mirrorsWithTheStructureItIsPlacedIn(final BlockMirror mirror, final Direction expected) {
    assertEquals(
        expected, block().getDefaultState().mirror(mirror).get(LightableFurnitureBlock.FACING));
  }
}
