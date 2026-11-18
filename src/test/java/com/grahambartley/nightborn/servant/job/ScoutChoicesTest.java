package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.structure.NearestStructures.Found;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScoutChoicesTest {

  private static final Found NEAR = found("crypt", 50, 0);
  private static final Found MIDDLE = found("village_plains", 0, -200);
  private static final Found FAR = found("castle", 300, 300);

  static Found found(final String path, final int x, final int z) {
    return new Found(Identifier.of("nightborn", path), new BlockPos(x, 0, z));
  }

  @ParameterizedTest(name = "range {0}, most {1} -> {2}")
  @CsvSource({"1000, 16, 3", "256, 16, 2", "100, 16, 1", "1000, 2, 2", "10, 16, 0"})
  @DisplayName("the wheel offers the nearest places within range, nearest first, up to a ringful")
  void nearest(final int range, final int most, final int offered) {
    final List<Found> chosen =
        ScoutChoices.nearest(BlockPos.ORIGIN, List.of(FAR, NEAR, MIDDLE, NEAR), range, most);
    assertEquals(List.of(NEAR, MIDDLE, FAR).subList(0, offered), chosen);
  }

  @ParameterizedTest(name = "({0}, {1}, {2}) -> {3}")
  @CsvSource({"3, 100, 4, 25", "0, -60, 0, 0", "-3, 0, -4, 25"})
  @DisplayName("distance to a place ignores height, since a structure is found by its column")
  void flat(final int x, final int y, final int z, final double squared) {
    assertEquals(squared, ScoutChoices.flatDistanceSquared(BlockPos.ORIGIN, new BlockPos(x, y, z)));
  }
}
