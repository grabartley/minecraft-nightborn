package com.grahambartley.nightborn.tools.structures.nbt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class NbtListTest {

  @Test
  void intsAreAnIntList() {
    final NbtList list = NbtList.ints(1, 2, 3);
    assertEquals(Nbt.INT, list.kind());
    assertEquals(List.of(1, 2, 3), list.items());
  }

  @Test
  void doublesAreADoubleList() {
    final NbtList list = NbtList.doubles(0.5, 2);
    assertEquals(Nbt.DOUBLE, list.kind());
    assertEquals(List.of(0.5, 2.0), list.items());
  }

  @Test
  void stringsAndCompoundsKeepTheirKind() {
    assertEquals(Nbt.STRING, NbtList.strings(List.of("a")).kind());
    assertEquals(Nbt.COMPOUND, NbtList.compounds(List.of(Nbt.compound())).kind());
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
