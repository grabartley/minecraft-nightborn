package com.grahambartley.nightborn.tools.structures.nbt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class NbtReaderTest {

  static Stream<Arguments> values() {
    return Stream.of(
        Arguments.of("byte", (byte) -3),
        Arguments.of("short", (short) 300),
        Arguments.of("int", 70000),
        Arguments.of("long", 1L << 40),
        Arguments.of("float", 1.5f),
        Arguments.of("double", 0.25),
        Arguments.of("bytes", new byte[] {1, 2}),
        Arguments.of("text", "lantern"),
        Arguments.of("list", NbtList.strings(List.of("a", "b"))),
        Arguments.of("compound", Nbt.compound("inner", 1)),
        Arguments.of("ints", new int[] {4, 5}),
        Arguments.of("longs", new long[] {6L}));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("values")
  void readsBackEveryTagTypeTheWriterWrites(final String key, final Object value) {
    final byte[] written = NbtWriter.bytes(Nbt.compound(key, value));
    assertArrayEquals(written, NbtWriter.bytes(NbtReader.read(written)));
  }

  @ParameterizedTest(name = "keys {0}")
  @ValueSource(strings = {"z,a,m", "a,b", "only"})
  void keepsTheOrderOfCompoundKeys(final String keys) {
    final Map<String, Object> root = Nbt.compound();
    final List<String> order = List.of(keys.split(","));
    order.forEach(key -> root.put(key, 1));
    assertEquals(order, List.copyOf(NbtReader.read(NbtWriter.bytes(root)).keySet()));
  }

  @ParameterizedTest(name = "root tag {0}")
  @ValueSource(bytes = {1, 3, 8, 9})
  void rejectsARootThatIsNotACompound(final byte tag) {
    assertThrows(IllegalArgumentException.class, () -> NbtReader.read(new byte[] {tag, 0, 0}));
  }
}
