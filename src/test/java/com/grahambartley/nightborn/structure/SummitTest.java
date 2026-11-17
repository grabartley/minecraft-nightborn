package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.util.math.ColumnPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class SummitTest {

  private static final ColumnPos START = new ColumnPos(160, -320);

  private static ToIntFunction<ColumnPos> cone(final ColumnPos peak, final int height) {
    return point ->
        height - Math.max(Math.abs(point.x() - peak.x()), Math.abs(point.z() - peak.z()));
  }

  static Stream<Arguments> mountains() {
    final ToIntFunction<ColumnPos> flat = point -> 120;
    return Stream.of(
        Arguments.of(
            "a peak north east of the start",
            new Summit(48, 12, 100, 320),
            cone(new ColumnPos(184, -344), 200),
            Optional.of(new ColumnPos(184, -344))),
        Arguments.of(
            "a peak between samples takes the nearest sample to it",
            new Summit(48, 12, 100, 320),
            cone(new ColumnPos(189, -344), 200),
            Optional.of(new ColumnPos(184, -344))),
        Arguments.of(
            "a peak beyond reach takes the highest ground in reach",
            new Summit(48, 12, 100, 320),
            cone(new ColumnPos(400, -320), 300),
            Optional.of(new ColumnPos(208, -320))),
        Arguments.of(
            "level ground keeps the start", new Summit(48, 12, 100, 320), flat, Optional.of(START)),
        Arguments.of(
            "a hill under the lowest height is refused",
            new Summit(48, 12, 130, 320),
            cone(START, 129),
            Optional.empty()),
        Arguments.of(
            "a hill at the lowest height is taken",
            new Summit(48, 12, 130, 320),
            cone(START, 130),
            Optional.of(START)),
        Arguments.of(
            "a peak over the highest height gives way to the highest ground under it",
            new Summit(48, 12, 100, 250),
            cone(START, 251),
            Optional.of(new ColumnPos(148, -320))),
        Arguments.of(
            "ground that is all over the highest height is refused",
            new Summit(48, 12, 100, 250),
            (ToIntFunction<ColumnPos>) point -> 300,
            Optional.empty()),
        Arguments.of(
            "a peak at the highest height is taken",
            new Summit(48, 12, 100, 250),
            cone(START, 250),
            Optional.of(START)),
        Arguments.of(
            "no reach keeps the start",
            new Summit(0, 8, 0, 320),
            cone(new ColumnPos(170, -320), 200),
            Optional.of(START)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("mountains")
  @DisplayName("a summit is the highest sampled ground in reach, if it is high enough")
  void peak(
      final String name,
      final Summit summit,
      final ToIntFunction<ColumnPos> heights,
      final Optional<ColumnPos> peak) {
    assertEquals(peak, summit.peak(START, heights));
  }

  @ParameterizedTest(name = "reach {0}, spacing {1}: {2} samples")
  @CsvSource({"48, 12, 81", "48, 16, 49", "0, 8, 1", "10, 8, 10", "64, 1, 16641"})
  @DisplayName("samples cover the reach on a grid and always include the start")
  void samples(final int reach, final int spacing, final int count) {
    final List<ColumnPos> samples = new Summit(reach, spacing, 0, 320).samples(START);
    assertEquals(count, samples.size());
    assertTrue(samples.contains(START));
    assertTrue(
        samples.stream()
            .allMatch(
                point ->
                    Math.abs(point.x() - START.x()) <= reach
                        && Math.abs(point.z() - START.z()) <= reach));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"reach\": 48, \"spacing\": 12, \"min_height\": 100} | true",
        "{\"reach\": 0} | true",
        "{\"reach\": 64} | true",
        "{\"reach\": 65} | false",
        "{\"reach\": -1} | false",
        "{\"reach\": 16, \"spacing\": 0} | false",
        "{\"reach\": 16, \"spacing\": 17} | false",
        "{\"reach\": 16, \"min_height\": 321} | false",
        "{\"reach\": 16, \"max_height\": 250} | true",
        "{\"reach\": 16, \"max_height\": 321} | false",
        "{} | false"
      })
  @DisplayName("a summit search reaches at most 64 blocks on a grid of 1 to 16")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, Summit.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {"{\"reach\": 20} | 8 | -64 | 320"})
  @DisplayName("a summit search samples every 8 blocks and takes any height unless told otherwise")
  void defaults(final String json, final int spacing, final int minHeight, final int maxHeight) {
    final Summit summit =
        Summit.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(spacing, summit.spacing());
    assertEquals(minHeight, summit.minHeight());
    assertEquals(maxHeight, summit.maxHeight());
  }
}
