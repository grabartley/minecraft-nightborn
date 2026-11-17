package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HeadSeatTest {

  @ParameterizedTest(name = "{0} reads as {1}")
  @CsvSource({"lowest, LOWEST", "highest, HIGHEST"})
  @DisplayName("a seat reads from data by its name")
  void reads(final String id, final HeadSeat seat) {
    assertEquals(seat, HeadSeat.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(id)).getOrThrow());
    assertEquals(id, seat.asString());
  }

  @ParameterizedTest(name = "{0} picks the floor at y {1}")
  @CsvSource({"LOWEST, 33", "HIGHEST, 69"})
  @DisplayName("a seat orders the floors of one column so its own comes first")
  void orders(final HeadSeat seat, final int y) {
    final List<BlockPos> floors =
        Stream.of(45, 69, 33, 51).map(floor -> new BlockPos(4, floor, 4)).toList();
    assertEquals(y, floors.stream().min(seat.order()).orElseThrow().getY());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"middle", "HIGHEST", "top"})
  @DisplayName("a name that is not a seat is rejected")
  void rejects(final String id) {
    assertEquals(false, HeadSeat.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(id)).isSuccess());
  }
}
