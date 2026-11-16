package com.grahambartley.nightborn.tools.structures.files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import com.grahambartley.nightborn.tools.structures.nbt.NbtWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GeneratedSyncTest {

  private static final FileTime OLD = FileTime.fromMillis(1_000_000L);
  private static final List<String> OWNED = List.of("structure");

  @TempDir Path generated;
  @TempDir Path target;

  private OutputTree ours;

  @BeforeEach
  void generate() {
    ours = new OutputTree(generated);
    ours.nbt("structure/a.nbt", Nbt.compound("a", 1));
    ours.nbt("structure/t.nbt", Nbt.compound("x", 1));
  }

  @Test
  void copiesMissingAndChangedFiles() {
    new OutputTree(target).nbt("structure/a.nbt", Nbt.compound("a", 2));
    final GeneratedSync.Result result =
        GeneratedSync.apply(generated, ours.written(), target, OWNED);
    assertEquals(new GeneratedSync.Result(2, 0), result);
    assertArrayEquals(
        NbtWriter.bytes(Nbt.compound("a", 1)), NbtFiles.payload(target.resolve("structure/a.nbt")));
    assertArrayEquals(
        NbtWriter.bytes(Nbt.compound("x", 1)), NbtFiles.payload(target.resolve("structure/t.nbt")));
  }

  @Test
  void leavesFilesThatAlreadyMatchAlone() throws IOException {
    GeneratedSync.apply(generated, ours.written(), target, OWNED);
    Files.setLastModifiedTime(target.resolve("structure/t.nbt"), OLD);
    final GeneratedSync.Result result =
        GeneratedSync.apply(generated, ours.written(), target, OWNED);
    assertEquals(new GeneratedSync.Result(0, 0), result);
    assertEquals(OLD, Files.getLastModifiedTime(target.resolve("structure/t.nbt")));
  }

  @Test
  void deletesOwnedFilesNothingWroteAndKeepsTheRest() throws IOException {
    Files.createDirectories(target.resolve("structure"));
    Files.createDirectories(target.resolve("loot"));
    Files.writeString(target.resolve("structure/stray.nbt"), "");
    Files.writeString(target.resolve("loot/keep.json"), "{}");
    final GeneratedSync.Result result =
        GeneratedSync.apply(generated, ours.written(), target, OWNED);
    assertEquals(1, result.deleted());
    assertTrue(Files.notExists(target.resolve("structure/stray.nbt")));
    assertTrue(Files.exists(target.resolve("loot/keep.json")));
  }
}
