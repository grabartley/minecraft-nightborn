package com.grahambartley.nightborn.tools.structures.nbt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NbtReaderTest {

  @Test
  void readsBackEveryTagTypeTheWriterWrites() {
    final Map<String, Object> root =
        Nbt.compound(
            "byte",
            (byte) -3,
            "short",
            (short) 300,
            "int",
            70000,
            "long",
            1L << 40,
            "float",
            1.5f,
            "double",
            0.25,
            "bytes",
            new byte[] {1, 2},
            "text",
            "lantern",
            "list",
            NbtList.strings(List.of("a", "b")),
            "compound",
            Nbt.compound("inner", 1),
            "ints",
            new int[] {4, 5},
            "longs",
            new long[] {6L});
    final byte[] written = NbtWriter.bytes(root);
    assertArrayEquals(written, NbtWriter.bytes(NbtReader.read(written)));
  }

  @Test
  void keepsTheOrderOfCompoundKeys() {
    final Map<String, Object> read =
        NbtReader.read(NbtWriter.bytes(Nbt.compound("z", 1, "a", 2, "m", 3)));
    assertEquals(List.of("z", "a", "m"), List.copyOf(read.keySet()));
  }

  @Test
  void rejectsARootThatIsNotACompound() {
    assertThrows(IllegalArgumentException.class, () -> NbtReader.read(new byte[] {8, 0, 0}));
  }
}
