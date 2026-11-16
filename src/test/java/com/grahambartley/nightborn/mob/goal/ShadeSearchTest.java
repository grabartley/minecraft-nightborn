package com.grahambartley.nightborn.mob.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.util.math.Vec3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShadeSearchTest {

  private static final List<Vec3i> LINE =
      IntStream.range(0, 10).mapToObj(x -> new Vec3i(x, 0, 0)).toList();

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

  @ParameterizedTest(name = "{0} comes before {1}")
  @CsvSource({
    "'1,0,0', '2,0,0'",
    "'1,0,1', '2,0,0'",
    "'2,0,0', '0,0,3'",
    "'1,0,0', '1,1,0'",
    "'1,1,0', '1,-1,0'",
    "'1,-1,0', '1,2,0'",
    "'-1,0,0', '1,0,0'",
    "'0,0,-1', '0,0,1'"
  })
  @DisplayName("breaks ties by height, upward first, then by x and z")
  void tieBreaks(final String first, final String second) {
    final List<Vec3i> offsets = ShadeSearch.nearestFirst(3, 2);
    assertTrue(offsets.indexOf(vec(first)) < offsets.indexOf(vec(second)));
  }

  @ParameterizedTest(name = "shaded {0}, reachable {1}, batch {2} x {3}: picks {4}")
  @CsvSource({
    "'0,5,9', '0,5,9', 1, 4, 0",
    "'5,9', '5,9', 1, 4, 5",
    "'0,5,9', '5,9', 1, 4, 5",
    "'0,5,9', '9', 1, 4, 9",
    "'0,5,9', '9', 1, 2, -1",
    "'0,5,9', '9', 3, 1, 9",
    "'0,1,2,3,4,5', '5', 2, 3, 5",
    "'0,1,2,3,4,5', '5', 2, 2, -1",
    "'0,5,9', '', 4, 4, -1",
    "'', '0,5,9', 4, 4, -1"
  })
  @DisplayName("routes to the first reachable batch of the nearest shade, giving up after a few")
  void nearestReachable(
      final String shaded,
      final String reachable,
      final int batchSize,
      final int batches,
      final int picked) {
    final Set<Integer> shade = ints(shaded);
    final Set<Integer> reach = ints(reachable);
    final Optional<Integer> found =
        ShadeSearch.nearestReachable(
            LINE,
            offset -> shade.contains(offset.getX()),
            new HashSet<>(),
            spots -> spots.stream().map(Vec3i::getX).filter(reach::contains).findFirst(),
            batchSize,
            batches);
    assertEquals(picked, found.orElse(-1));
  }

  @ParameterizedTest(name = "batch {0}: first search tries {1}, second tries {2}")
  @CsvSource({"2, '0,1', '2,3'", "4, '0,1,2,3', '4,5'", "6, '0,1,2,3,4,5', ''"})
  @DisplayName("remembers shade it could not reach and moves outward on the next search")
  void remembersUnreachableShade(final int batchSize, final String first, final String second) {
    final Set<Vec3i> ruledOut = new HashSet<>();
    final List<String> tried = new ArrayList<>(List.of("", ""));
    for (int search = 0; search < 2; search++) {
      final int index = search;
      ShadeSearch.nearestReachable(
          LINE,
          offset -> offset.getX() <= 5,
          ruledOut,
          spots -> {
            tried.set(
                index,
                spots.stream()
                    .map(spot -> String.valueOf(spot.getX()))
                    .collect(Collectors.joining(",")));
            return Optional.<Integer>empty();
          },
          batchSize,
          1);
    }
    assertEquals(List.of(first, second == null ? "" : second), tried);
  }

  private static int flat(final Vec3i offset) {
    return offset.getX() * offset.getX() + offset.getZ() * offset.getZ();
  }

  private static Vec3i vec(final String csv) {
    final int[] parts = Arrays.stream(csv.split(",")).mapToInt(Integer::parseInt).toArray();
    return new Vec3i(parts[0], parts[1], parts[2]);
  }

  private static Set<Integer> ints(final String csv) {
    return csv == null || csv.isBlank()
        ? Set.of()
        : Arrays.stream(csv.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
  }
}
