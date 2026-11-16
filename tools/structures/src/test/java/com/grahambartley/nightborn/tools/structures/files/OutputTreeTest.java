package com.grahambartley.nightborn.tools.structures.files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import com.grahambartley.nightborn.tools.structures.nbt.NbtWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OutputTreeTest {

  @TempDir Path root;

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"t.nbt", "a/b/c.nbt", "structure/hunter_camp/camp.nbt"})
  void writesGzippedNbtThatReadsBackAsTheSamePayload(final String path) {
    new OutputTree(root).nbt(path, Nbt.compound("x", 1));
    assertArrayEquals(NbtWriter.bytes(Nbt.compound("x", 1)), NbtFiles.payload(root.resolve(path)));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"a.nbt", "a/b.nbt"})
  void refusesASecondWriteToTheSamePath(final String path) {
    final OutputTree tree = new OutputTree(root);
    tree.nbt(path, Nbt.compound());
    assertThrows(IllegalStateException.class, () -> tree.nbt(path, Nbt.compound("x", 1)));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"a.nbt", "a.nbt b/c.nbt", "x/y.nbt x/z.nbt w.nbt"})
  void remembersEveryFileItWasAskedToWrite(final String paths) {
    final OutputTree tree = new OutputTree(root);
    Arrays.stream(paths.split(" ")).forEach(path -> tree.nbt(path, Nbt.compound()));
    assertEquals(Set.of(paths.split(" ")), tree.written());
  }
}
