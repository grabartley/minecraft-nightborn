package com.grahambartley.nightborn.client.gramophone;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GramophoneEarshotTest {

  static Stream<Arguments> cases() {
    final Vec3d listener = new Vec3d(0.5, 64.5, 0.5);
    return Stream.of(
        Arguments.of(List.of(), listener, false),
        Arguments.of(List.of(new BlockPos(0, 64, 0)), listener, true),
        Arguments.of(List.of(new BlockPos(64, 64, 0)), listener, true),
        Arguments.of(List.of(new BlockPos(65, 64, 0)), listener, false),
        Arguments.of(List.of(new BlockPos(0, 0, 0)), listener, true),
        Arguments.of(List.of(new BlockPos(46, 64, 46)), listener, false),
        Arguments.of(List.of(new BlockPos(300, 64, 0), new BlockPos(10, 70, -10)), listener, true));
  }

  @ParameterizedTest(name = "gramophones at {0}: heard {2}")
  @MethodSource("cases")
  @DisplayName("background music yields only to a gramophone playing within earshot")
  void hears(final List<BlockPos> playing, final Vec3d listener, final boolean heard) {
    assertEquals(heard, GramophoneEarshot.hears(playing, listener));
  }
}
