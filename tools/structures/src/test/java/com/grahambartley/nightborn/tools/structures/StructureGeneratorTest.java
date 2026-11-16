package com.grahambartley.nightborn.tools.structures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.files.GeneratedSync;
import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import com.grahambartley.nightborn.tools.structures.nbt.NbtWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StructureGeneratorTest {

  private static final Path REPO = Path.of("../..").toAbsolutePath().normalize();
  private static final Path DATA = REPO.resolve("src/main/resources/data/nightborn");
  private static final String CAMP = "structure/hunter_camp/camp.nbt";
  private static final FileTime OLD = FileTime.fromMillis(1_000_000L);

  @TempDir Path scratch;

  private Path copyOfShippedData() throws IOException {
    final Path root = scratch.resolve("data");
    for (final String folder : StructureGenerator.OWNED_FOLDERS) {
      final Path from = DATA.resolve(folder);
      try (Stream<Path> walk = Files.walk(from)) {
        for (final Path file : walk.filter(Files::isRegularFile).toList()) {
          final Path to = root.resolve(DATA.relativize(file).toString());
          Files.createDirectories(to.getParent());
          Files.copy(file, to);
        }
      }
    }
    return root;
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "write a", "check a", "generate a b", "write a b c"})
  void printsUsageForUnknownArguments(final String args) {
    assertEquals(
        List.of(StructureGenerator.USAGE),
        StructureGenerator.run(args.isEmpty() ? new String[0] : args.split(" ")));
  }

  @Test
  void theShippedTemplatesMatchTheGenerator() {
    assertEquals(List.of(), StructureGenerator.check(DATA, scratch.resolve("out")));
  }

  @Test
  void writeLeavesAnUpToDateTreeAloneAndDeletesStrayFiles() throws IOException {
    final Path root = copyOfShippedData();
    final Path kept = root.resolve(CAMP);
    Files.setLastModifiedTime(kept, OLD);
    Files.writeString(root.resolve("structure/stray.nbt"), "");
    assertEquals(List.of(), StructureGenerator.write(root, scratch.resolve("out")));
    assertEquals(OLD, Files.getLastModifiedTime(kept));
    assertTrue(Files.notExists(root.resolve("structure/stray.nbt")));
  }

  @Test
  void writeRestoresAChangedTemplate() throws IOException {
    final Path root = copyOfShippedData();
    final Path changed = root.resolve(CAMP);
    Files.write(changed, NbtFiles.compressed(NbtWriter.bytes(Nbt.compound("x", 1))));
    StructureGenerator.write(root, scratch.resolve("out"));
    assertArrayEquals(NbtFiles.payload(DATA.resolve(CAMP)), NbtFiles.payload(changed));
  }

  @Test
  void checkReportsAChangedTemplate() throws IOException {
    final Path root = copyOfShippedData();
    Files.write(root.resolve(CAMP), NbtFiles.compressed(NbtWriter.bytes(Nbt.compound("x", 1))));
    assertEquals(
        List.of("differs: " + CAMP), StructureGenerator.check(root, scratch.resolve("out")));
  }

  @Test
  void summaryCountsGeneratedRewrittenAndDeletedFiles() {
    assertEquals(
        "Generated 3 files, rewrote 2 and deleted 1.",
        StructureGenerator.summary(3, new GeneratedSync.Result(2, 1)));
  }

  @Test
  void checkStartsFromAnEmptyScratchFolder() throws IOException {
    final Path out = scratch.resolve("out");
    Files.createDirectories(out.resolve("structure"));
    Files.writeString(out.resolve("structure/leftover.nbt"), "");
    StructureGenerator.check(DATA, out);
    assertTrue(Files.notExists(out.resolve("structure/leftover.nbt")));
  }
}
