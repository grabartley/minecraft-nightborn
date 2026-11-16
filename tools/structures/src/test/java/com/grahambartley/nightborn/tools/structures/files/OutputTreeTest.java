package com.grahambartley.nightborn.tools.structures.files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import com.grahambartley.nightborn.tools.structures.nbt.NbtWriter;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OutputTreeTest {

  @TempDir Path root;

  @Test
  void writesGzippedNbtThatReadsBackAsTheSamePayload() {
    new OutputTree(root).nbt("t.nbt", Nbt.compound("x", 1));
    assertArrayEquals(
        NbtWriter.bytes(Nbt.compound("x", 1)), NbtFiles.payload(root.resolve("t.nbt")));
  }

  @Test
  void refusesASecondWriteToTheSamePath() {
    final OutputTree tree = new OutputTree(root);
    tree.nbt("a/b.nbt", Nbt.compound());
    assertThrows(IllegalStateException.class, () -> tree.nbt("a/b.nbt", Nbt.compound("x", 1)));
  }

  @Test
  void remembersEveryFileItWasAskedToWrite() {
    final OutputTree tree = new OutputTree(root);
    tree.nbt("b/c.nbt", Nbt.compound());
    tree.nbt("a.nbt", Nbt.compound());
    assertEquals(Set.of("a.nbt", "b/c.nbt"), tree.written());
  }
}
