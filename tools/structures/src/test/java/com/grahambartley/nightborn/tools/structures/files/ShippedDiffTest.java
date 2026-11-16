package com.grahambartley.nightborn.tools.structures.files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ShippedDiffTest {

  @TempDir Path generated;
  @TempDir Path shipped;

  private OutputTree ours;

  @BeforeEach
  void writeBothSides() {
    ours = new OutputTree(generated);
    ours.nbt("structure/a.nbt", Nbt.compound("a", 1));
    ours.nbt("structure/t.nbt", Nbt.compound("x", 1));
    final OutputTree theirs = new OutputTree(shipped);
    theirs.nbt("structure/a.nbt", Nbt.compound("a", 1));
    theirs.nbt("structure/t.nbt", Nbt.compound("x", 1));
  }

  @Test
  void findsNothingWhenEveryFileMatches() {
    assertEquals(List.of(), diff());
  }

  @Test
  void reportsATemplateWhosePayloadDiffers() {
    new OutputTree(shipped).nbt("structure/t.nbt", Nbt.compound("x", 2));
    assertEquals(List.of("differs: structure/t.nbt"), diff());
  }

  @Test
  void reportsAGeneratedFileThatIsNotShipped() {
    ours.nbt("structure/new.nbt", Nbt.compound());
    assertEquals(List.of("not shipped: structure/new.nbt"), diff());
  }

  @Test
  void reportsAShippedFileInAnOwnedFolderThatWasNotGenerated() throws IOException {
    Files.writeString(shipped.resolve("structure/stray.nbt"), "");
    assertEquals(List.of("not generated: structure/stray.nbt"), diff());
  }

  @Test
  void ignoresShippedFilesOutsideTheOwnedFolders() throws IOException {
    Files.createDirectories(shipped.resolve("loot"));
    Files.writeString(shipped.resolve("loot/x.json"), "{}");
    assertEquals(List.of(), diff());
  }

  private List<String> diff() {
    return ShippedDiff.differences(
        generated, ours.written(), shipped, List.of("structure", "missing"));
  }
}
