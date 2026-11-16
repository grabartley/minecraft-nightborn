package com.grahambartley.nightborn.tools.structures.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OwnedFilesTest {

  @TempDir Path root;

  @BeforeEach
  void writeFiles() throws IOException {
    for (final String file : List.of("structure/b.nbt", "structure/x/a.nbt", "loot/c.json")) {
      Files.createDirectories(root.resolve(file).getParent());
      Files.writeString(root.resolve(file), "");
    }
  }

  @Test
  void listsEveryFileUnderTheOwnedFoldersSorted() {
    assertEquals(
        List.of("structure/b.nbt", "structure/x/a.nbt"),
        OwnedFiles.list(root, List.of("structure", "missing")));
  }

  @Test
  void staleFilesAreOwnedFilesNobodyWrote() {
    assertEquals(
        List.of("structure/b.nbt"),
        OwnedFiles.stale(root, List.of("structure"), Set.of("structure/x/a.nbt")));
  }

  @Test
  void deleteRemovesOnlyTheNamedFiles() {
    OwnedFiles.delete(root, List.of("structure/b.nbt"));
    assertTrue(Files.notExists(root.resolve("structure/b.nbt")));
    assertTrue(Files.exists(root.resolve("loot/c.json")));
  }
}
