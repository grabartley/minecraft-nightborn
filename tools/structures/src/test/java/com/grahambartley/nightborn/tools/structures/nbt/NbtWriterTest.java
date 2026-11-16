package com.grahambartley.nightborn.tools.structures.nbt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NbtWriterTest {

  static Stream<Arguments> entries() {
    return Stream.of(
        Arguments.of(
            "a byte", Nbt.compound("a", (byte) 1), new byte[] {10, 0, 0, 1, 0, 1, 'a', 1, 0}),
        Arguments.of(
            "an int", Nbt.compound("b", 2), new byte[] {10, 0, 0, 3, 0, 1, 'b', 0, 0, 0, 2, 0}),
        Arguments.of(
            "a string", Nbt.compound("c", "x"), new byte[] {10, 0, 0, 8, 0, 1, 'c', 0, 1, 'x', 0}),
        Arguments.of(
            "an int list",
            Nbt.compound("d", NbtList.ints(7)),
            new byte[] {10, 0, 0, 9, 0, 1, 'd', 3, 0, 0, 0, 1, 0, 0, 0, 7, 0}),
        Arguments.of(
            "an empty compound list",
            Nbt.compound("e", NbtList.compounds(List.of())),
            new byte[] {10, 0, 0, 9, 0, 1, 'e', 10, 0, 0, 0, 0, 0}),
        Arguments.of(
            "a boolean as a byte",
            Nbt.compound("f", true),
            new byte[] {10, 0, 0, 1, 0, 1, 'f', 1, 0}));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("entries")
  void writesANamelessRootCompoundWithTypedEntries(
      final String name, final java.util.Map<String, Object> root, final byte[] expected) {
    assertArrayEquals(expected, NbtWriter.bytes(root));
  }

  @Test
  void rejectsAValueThatIsNotNbt() {
    assertThrows(
        IllegalArgumentException.class, () -> NbtWriter.bytes(Nbt.compound("x", new Object())));
  }
}
