package com.grahambartley.nightborn.tools.structures.nbt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NbtWriterTest {

  @Test
  void writesANamelessRootCompoundWithTypedEntries() {
    final Map<String, Object> root =
        Nbt.compound("a", (byte) 1, "b", 2, "c", "x", "d", NbtList.ints(7));
    final byte[] expected = {
      10, 0, 0, 1, 0, 1, 'a', 1, 3, 0, 1, 'b', 0, 0, 0, 2, 8, 0, 1, 'c', 0, 1, 'x', 9, 0, 1, 'd', 3,
      0, 0, 0, 1, 0, 0, 0, 7, 0
    };
    assertArrayEquals(expected, NbtWriter.bytes(root));
  }

  @Test
  void writesAnEmptyListWithItsDeclaredKind() {
    final byte[] expected = {10, 0, 0, 9, 0, 1, 'e', 10, 0, 0, 0, 0, 0};
    assertArrayEquals(expected, NbtWriter.bytes(Nbt.compound("e", NbtList.compounds(List.of()))));
  }

  @Test
  void writesABooleanAsAByte() {
    assertArrayEquals(
        NbtWriter.bytes(Nbt.compound("f", (byte) 1)), NbtWriter.bytes(Nbt.compound("f", true)));
  }

  @Test
  void rejectsAValueThatIsNotNbt() {
    assertThrows(
        IllegalArgumentException.class, () -> NbtWriter.bytes(Nbt.compound("x", new Object())));
  }
}
