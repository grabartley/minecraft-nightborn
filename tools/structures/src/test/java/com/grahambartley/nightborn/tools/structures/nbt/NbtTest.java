package com.grahambartley.nightborn.tools.structures.nbt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NbtTest {

  static Stream<Arguments> values() {
    return Stream.of(
        Arguments.of(Nbt.compound(), Nbt.COMPOUND),
        Arguments.of(NbtList.ints(), Nbt.LIST),
        Arguments.of("x", Nbt.STRING),
        Arguments.of(1.0, Nbt.DOUBLE),
        Arguments.of(1.0f, Nbt.FLOAT),
        Arguments.of(1L, Nbt.LONG),
        Arguments.of((short) 1, Nbt.SHORT),
        Arguments.of((byte) 1, Nbt.BYTE),
        Arguments.of(true, Nbt.BYTE),
        Arguments.of(1, Nbt.INT),
        Arguments.of(new byte[0], Nbt.BYTE_ARRAY),
        Arguments.of(new int[0], Nbt.INT_ARRAY),
        Arguments.of(new long[0], Nbt.LONG_ARRAY));
  }

  @ParameterizedTest
  @MethodSource("values")
  void mapsEachJavaTypeToItsTag(final Object value, final int tag) {
    assertEquals(tag, Nbt.tagId(value));
  }

  @Test
  void compoundKeepsKeyOrder() {
    assertEquals(List.of("b", "a"), List.copyOf(Nbt.compound("b", 1, "a", 2).keySet()));
  }

  @Test
  void compoundNeedsAValueForEveryKey() {
    assertThrows(IllegalArgumentException.class, () -> Nbt.compound("lonely"));
  }

  @Test
  void flagIsAByte() {
    assertEquals((byte) 1, Nbt.flag(true));
    assertEquals((byte) 0, Nbt.flag(false));
  }
}
