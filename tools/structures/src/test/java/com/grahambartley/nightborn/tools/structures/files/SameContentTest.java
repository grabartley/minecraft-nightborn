package com.grahambartley.nightborn.tools.structures.files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import com.grahambartley.nightborn.tools.structures.nbt.NbtWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SameContentTest {

  @TempDir Path folder;

  @Test
  void templatesMatchByPayloadWhateverTheirCompression() throws IOException {
    final byte[] payload = NbtWriter.bytes(Nbt.compound("x", 1));
    final Path ours = folder.resolve("a.nbt");
    final Path theirs = folder.resolve("b.nbt");
    Files.write(ours, NbtFiles.compressed(payload));
    final ByteArrayOutputStream other = new ByteArrayOutputStream();
    try (GZIPOutputStream out =
        new GZIPOutputStream(other) {
          {
            def.setLevel(Deflater.BEST_COMPRESSION);
          }
        }) {
      out.write(payload);
    }
    Files.write(theirs, other.toByteArray());
    assertTrue(SameContent.of(ours, theirs));
  }

  @Test
  void templatesDifferWhenThePayloadDoes() throws IOException {
    final Path ours = folder.resolve("a.nbt");
    final Path theirs = folder.resolve("b.nbt");
    Files.write(ours, NbtFiles.compressed(NbtWriter.bytes(Nbt.compound("x", 1))));
    Files.write(theirs, NbtFiles.compressed(NbtWriter.bytes(Nbt.compound("x", 2))));
    assertFalse(SameContent.of(ours, theirs));
  }

  @Test
  void otherFilesMatchByteForByte() throws IOException {
    Files.writeString(folder.resolve("a.json"), "{}");
    Files.writeString(folder.resolve("b.json"), "{ }");
    Files.writeString(folder.resolve("c.json"), "{}");
    assertFalse(SameContent.of(folder.resolve("a.json"), folder.resolve("b.json")));
    assertTrue(SameContent.of(folder.resolve("a.json"), folder.resolve("c.json")));
  }
}
