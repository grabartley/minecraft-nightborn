package com.grahambartley.nightborn.tools.structures.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OwnedFilesTest {

  @TempDir Path root;

  @BeforeEach
  void writeFiles() throws IOException {
    for (final String file : List.of("structure/b.nbt", "structure/x/a.nbt", "loot/c.json")) {
      Files.createDirectories(root.resolve(file).getParent());
      Files.writeString(root.resolve(file), "");
    }
  }

  private static List<String> split(final String list) {
    return list.isEmpty() ? List.of() : Arrays.asList(list.split(" "));
  }

  @ParameterizedTest(name = "owning [{0}] lists [{1}]")
  @CsvSource({
    "structure missing, structure/b.nbt structure/x/a.nbt",
    "loot, loot/c.json",
    "missing, ''",
    "structure/x, structure/x/a.nbt"
  })
  void listsEveryFileUnderTheOwnedFoldersSorted(final String folders, final String files) {
    assertEquals(split(files), OwnedFiles.list(root, split(folders)));
  }

  @ParameterizedTest(name = "wrote [{0}], stale [{1}]")
  @CsvSource({
    "structure/x/a.nbt, structure/b.nbt",
    "structure/b.nbt structure/x/a.nbt, ''",
    "'', structure/b.nbt structure/x/a.nbt"
  })
  void staleFilesAreOwnedFilesNobodyWrote(final String written, final String stale) {
    assertEquals(
        split(stale), OwnedFiles.stale(root, List.of("structure"), Set.copyOf(split(written))));
  }

  @ParameterizedTest(name = "deleting {0}")
  @CsvSource({"structure/b.nbt", "structure/x/a.nbt"})
  void deleteRemovesOnlyTheNamedFiles(final String file) {
    OwnedFiles.delete(root, List.of(file));
    assertTrue(Files.notExists(root.resolve(file)));
    assertTrue(Files.exists(root.resolve("loot/c.json")));
  }
}
