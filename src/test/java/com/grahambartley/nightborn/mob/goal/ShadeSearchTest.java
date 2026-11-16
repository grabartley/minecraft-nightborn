package com.grahambartley.nightborn.mob.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.math.Vec3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShadeSearchTest {

  @ParameterizedTest(name = "radius {0}, height {1}: {2} spots")
  @CsvSource({"0, 0, 1", "1, 0, 9", "1, 1, 27", "12, 4, 5625"})
  @DisplayName("covers every spot in the box exactly once")
  void coversTheBox(final int radius, final int height, final int spots) {
    final List<Vec3i> offsets = ShadeSearch.nearestFirst(radius, height);
    assertEquals(spots, offsets.size());
    assertEquals(spots, Set.copyOf(offsets).size());
  }

  @ParameterizedTest(name = "radius {0}, height {1}")
  @CsvSource({"1, 1", "3, 2", "12, 4"})
  @DisplayName("starts underfoot and never gets further away along the ground")
  void nearestFirst(final int radius, final int height) {
    final List<Vec3i> offsets = ShadeSearch.nearestFirst(radius, height);
    assertEquals(Vec3i.ZERO, offsets.getFirst());
    for (int i = 1; i < offsets.size(); i++) {
      assertTrue(flat(offsets.get(i - 1)) <= flat(offsets.get(i)), "out of order at " + i);
    }
  }

  @ParameterizedTest(name = "shaded {0}, reachable {1}, tries {2}: picks {3}")
  @CsvSource({
    "'0,5,9', '0,5,9', 4, 0",
    "'5,9', '5,9', 4, 5",
    "'0,5,9', '5,9', 4, 5",
    "'0,5,9', '9', 4, 9",
    "'0,5,9', '9', 2, -1",
    "'0,5,9', '', 4, -1",
    "'', '0,5,9', 4, -1"
  })
  @DisplayName("takes the nearest shade it can reach, giving up after a few routes")
  void nearestReachable(
      final String shaded, final String reachable, final int tries, final int picked) {
    final List<Vec3i> line =
        java.util.stream.IntStream.range(0, 10).mapToObj(x -> new Vec3i(x, 0, 0)).toList();
    final Set<Integer> shade = ints(shaded);
    final Set<Integer> reach = ints(reachable);
    final Optional<Integer> found =
        ShadeSearch.nearestReachable(
            line,
            offset -> shade.contains(offset.getX()),
            offset -> reach.contains(offset.getX()) ? Optional.of(offset.getX()) : Optional.empty(),
            tries);
    assertEquals(picked, found.orElse(-1));
  }

  private static int flat(final Vec3i offset) {
    return offset.getX() * offset.getX() + offset.getZ() * offset.getZ();
  }

  private static Set<Integer> ints(final String csv) {
    return csv == null || csv.isBlank()
        ? Set.of()
        : Arrays.stream(csv.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
  }
}
