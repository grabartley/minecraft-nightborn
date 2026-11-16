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
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GeneratedSyncTest {

  private static final FileTime OLD = FileTime.fromMillis(1_000_000L);
  private static final List<String> OWNED = List.of("structure");

  @TempDir Path generated;
  @TempDir Path target;

  @ParameterizedTest(name = "target {0}, stray {1}: rewrote {2}, deleted {3}")
  @CsvSource({
    "same, false, 0, 0",
    "changed, false, 1, 0",
    "missing, false, 1, 0",
    "same, true, 0, 1",
    "changed, true, 1, 1"
  })
  void makesTheTargetMatchWhatWasGenerated(
      final String state, final boolean stray, final int rewritten, final int deleted)
      throws IOException {
    final OutputTree ours = new OutputTree(generated);
    ours.nbt("structure/t.nbt", Nbt.compound("x", 1));
    final Path shipped = target.resolve("structure/t.nbt");
    if (!state.equals("missing")) {
      new OutputTree(target)
          .nbt("structure/t.nbt", Nbt.compound("x", state.equals("same") ? 1 : 2));
      Files.setLastModifiedTime(shipped, OLD);
    }
    if (stray) {
      Files.writeString(target.resolve("structure/stray.nbt"), "");
    }
    Files.createDirectories(target.resolve("loot"));
    Files.writeString(target.resolve("loot/keep.json"), "{}");
    assertEquals(
        new GeneratedSync.Result(rewritten, deleted),
        GeneratedSync.apply(generated, ours.written(), target, OWNED));
    assertArrayEquals(NbtWriter.bytes(Nbt.compound("x", 1)), NbtFiles.payload(shipped));
    assertEquals(rewritten == 0, Files.getLastModifiedTime(shipped).equals(OLD));
    assertTrue(Files.notExists(target.resolve("structure/stray.nbt")));
    assertTrue(Files.exists(target.resolve("loot/keep.json")));
  }
}
