package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CampLayoutTest {

  private static CampLayout tent(final BlockRotation rotation) {
    return new CampLayout(
        BlockPos.ORIGIN,
        List.of(
            new CampLayout.Tent(
                new BlockPos(0, 0, 3),
                rotation,
                List.of(
                    new CampLayout.Piece(
                        new BlockPos(1, 0, 2),
                        Blocks.BROWN_BED.getDefaultState().with(BedBlock.FACING, Direction.SOUTH)),
                    new CampLayout.Piece(
                        new BlockPos(0, 1, 0), Blocks.BROWN_WOOL.getDefaultState())))));
  }

  @ParameterizedTest(name = "turned {0}, the bed lands at {1},{2} facing {3}")
  @CsvSource({
    "NONE, 1, 5, SOUTH",
    "CLOCKWISE_180, -1, 1, NORTH",
    "CLOCKWISE_90, -2, 4, WEST",
    "COUNTERCLOCKWISE_90, 2, 2, EAST"
  })
  void turnsEachTentAboutItsOwnAnchor(
      final BlockRotation rotation, final int x, final int z, final Direction facing) {
    final CampLayout.Piece bed = tent(rotation).pieces().get(0);
    assertEquals(new BlockPos(x, 0, z), bed.pos());
    assertEquals(facing, bed.state().get(BedBlock.FACING));
  }

  @ParameterizedTest(name = "turned {0}, the camp spans x {1}..{2}, z {3}..{4}")
  @CsvSource({"NONE, 0, 1, 0, 5", "CLOCKWISE_180, -1, 0, 0, 3"})
  void boundsCoverTheFireAndEveryPiece(
      final BlockRotation rotation,
      final int minX,
      final int maxX,
      final int minZ,
      final int maxZ) {
    assertEquals(new CampSite.Bounds(minX, maxX, minZ, maxZ, 2), tent(rotation).bounds());
  }

  @Test
  void theShippedCampParsesWithThreeTentsAndThreeBeds() throws Exception {
    final CampLayout layout =
        CampLayout.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    Files.readString(
                        Path.of(
                            "src/main/resources/data/nightborn/nightborn/party_camp/hunter_party.json"))))
            .getOrThrow();
    assertEquals(3, layout.tents().size());
    assertEquals(
        3,
        layout.pieces().stream()
            .filter(piece -> piece.state().isOf(Blocks.BROWN_BED))
            .filter(piece -> piece.state().get(BedBlock.PART) == BedPart.HEAD)
            .count());
  }
}
