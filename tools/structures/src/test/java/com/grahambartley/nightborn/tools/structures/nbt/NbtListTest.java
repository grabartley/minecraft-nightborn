package com.grahambartley.nightborn.tools.structures.nbt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NbtListTest {

  static Stream<Arguments> lists() {
    return Stream.of(
        Arguments.of("ints", NbtList.ints(1, 2, 3), Nbt.INT, List.of(1, 2, 3)),
        Arguments.of("doubles", NbtList.doubles(0.5, 2), Nbt.DOUBLE, List.of(0.5, 2.0)),
        Arguments.of("strings", NbtList.strings(List.of("a")), Nbt.STRING, List.of("a")),
        Arguments.of(
            "compounds",
            NbtList.compounds(List.of(Nbt.compound())),
            Nbt.COMPOUND,
            List.of(Nbt.compound())),
        Arguments.of("empty ints", NbtList.ints(), Nbt.INT, List.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("lists")
  void eachFactoryKeepsItsKindAndItems(
      final String name, final NbtList list, final int kind, final List<Object> items) {
    assertEquals(kind, list.kind());
    assertEquals(items, list.items());
  }

  @Test
  void copiesItsItemsSoLaterChangesDoNotLeakIn() {
    final List<Object> items = new ArrayList<>(List.of(1));
    final NbtList list = new NbtList(Nbt.INT, items);
    items.add(2);
    assertEquals(List.of(1), list.items());
    assertThrows(UnsupportedOperationException.class, () -> list.items().add(3));
  }
}
